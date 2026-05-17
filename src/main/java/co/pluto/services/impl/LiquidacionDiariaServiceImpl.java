package co.pluto.services.impl;

import co.pluto.dto.request.liquidacion.EjecutarRangoDiarioRequestDto;
import co.pluto.dto.request.liquidacion.IniciarLiquidacionDiariaRequestDto;
import co.pluto.dto.response.liquidacion.LiquidacionDiariaDetalleItemDto;
import co.pluto.dto.response.liquidacion.LiquidacionDiariaResponseDto;
import co.pluto.dto.response.liquidacion.RangoLiquidacionDiariaResponseDto;
import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.services.impl.liquidacion.MotorLiquidacionDiariaService;
import co.pluto.services.interfaces.ILiquidacionDiariaService;
import co.pluto.utils.exception.BusinessException;
import co.pluto.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LiquidacionDiariaServiceImpl implements ILiquidacionDiariaService {

  private static final DateTimeFormatter FORMATO_PERIODO =
      DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("es", "CO"));

  private final ILiquidacionDiariaRepository        liqRepo;
  private final ILiquidacionDiariaDetalleRepository detalleRepo;
  private final IOperacionRepository                operacionRepo;
  private final ISaldoInicialRepository             saldoInicialRepo;
  private final IEmpresaRepository                  empresaRepo;
  private final IUsuarioRepository                  usuarioRepo;
  private final MotorLiquidacionDiariaService       motor;
  private final ContabilizacionService              contabilizacionService;

  @Override
  public LiquidacionDiariaResponseDto iniciar(IniciarLiquidacionDiariaRequestDto req) {
    LocalDate fecha = req.getFecha();
    if (liqRepo.existsByFecha(fecha))
      throw new BusinessException("Ya existe una liquidación diaria para la fecha " + fecha);

    LiquidacionDiariaEntity liq = LiquidacionDiariaEntity.builder()
        .fecha(fecha)
        .estado("BORRADOR")
        .totalInteresesLiquidados(BigDecimal.ZERO)
        .build();

    liq = liqRepo.save(liq);
    log.info("Liquidación diaria iniciada para {}", fecha);
    return toDto(liq, List.of());
  }

  @Override
  public LiquidacionDiariaResponseDto calcular(Long id) {
    LiquidacionDiariaEntity liq = findOrThrow(id);

    if ("APROBADA".equals(liq.getEstado()) || "CONTABILIZADA".equals(liq.getEstado()))
      throw new BusinessException("No se puede recalcular una liquidación diaria " + liq.getEstado());

    liq.setEstado("BORRADOR");
    motor.calcular(liq);
    liq.setEstado("PENDIENTE_APROBACION");
    liq = liqRepo.save(liq);

    return toDto(liq, detalleRepo.findAllByLiquidacionId(liq.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<LiquidacionDiariaResponseDto> listar() {
    return liqRepo.findAllByOrderByFechaDesc().stream()
        .map(l -> toDto(l, List.of()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public LiquidacionDiariaResponseDto obtener(Long id) {
    LiquidacionDiariaEntity liq = findOrThrow(id);
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  public LiquidacionDiariaResponseDto aprobar(Long id) {
    LiquidacionDiariaEntity liq = findOrThrow(id);

    if (!"PENDIENTE_APROBACION".equals(liq.getEstado()))
      throw new BusinessException("Solo se puede aprobar una liquidación diaria en estado PENDIENTE_APROBACION");

    UsuarioEntity usuario = currentUser();
    liq.setEstado("APROBADA");
    liq.setAprobadaAt(OffsetDateTime.now());
    liq.setAprobadaPor(usuario != null ? usuario.getId() : null);
    liq = liqRepo.save(liq);

    log.info("Liquidación diaria {} aprobada por {}", liq.getFecha(),
        usuario != null ? usuario.getNombre() : "sistema");
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  public LiquidacionDiariaResponseDto revertir(Long id) {
    LiquidacionDiariaEntity liq = findOrThrow(id);

    if ("APROBADA".equals(liq.getEstado()) || "CONTABILIZADA".equals(liq.getEstado()))
      throw new BusinessException("No se puede revertir una liquidación diaria " + liq.getEstado());

    motor.revertir(liq);
    liq.setEstado("BORRADOR");
    liq = liqRepo.save(liq);

    log.info("Liquidación diaria {} revertida a BORRADOR", liq.getFecha());
    return toDto(liq, List.of());
  }

  @Override
  public LiquidacionDiariaResponseDto marcarContabilizada(Long id) {
    LiquidacionDiariaEntity liq = findOrThrow(id);

    if (!"APROBADA".equals(liq.getEstado()))
      throw new BusinessException("Solo se puede contabilizar una liquidación diaria APROBADA");

    contabilizacionService.contabilizarLiquidacionDiaria(liq);

    liq.setEstado("CONTABILIZADA");
    liq = liqRepo.save(liq);
    log.info("Liquidación diaria {} contabilizada — asientos generados", liq.getFecha());
    return toDto(liq, detalleRepo.findAllByLiquidacionId(id));
  }

  @Override
  public RangoLiquidacionDiariaResponseDto ejecutarRango(EjecutarRangoDiarioRequestDto req) {
    LocalDate fechaDesde = req.getFechaDesde();
    LocalDate fechaHasta = req.getFechaHasta();

    if (fechaHasta.isBefore(fechaDesde))
      throw new BusinessException("fechaHasta debe ser mayor o igual a fechaDesde");
    if (java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) > 365)
      throw new BusinessException("El rango no puede superar 365 días");

    List<LiquidacionDiariaResponseDto> resultado = new ArrayList<>();
    int creadas  = 0;
    int omitidas = 0;

    for (LocalDate fecha = fechaDesde; !fecha.isAfter(fechaHasta); fecha = fecha.plusDays(1)) {
      if (liqRepo.existsByFecha(fecha)) {
        omitidas++;
        continue;
      }
      LiquidacionDiariaEntity liq = liqRepo.save(
          LiquidacionDiariaEntity.builder()
              .fecha(fecha)
              .estado("BORRADOR")
              .totalInteresesLiquidados(BigDecimal.ZERO)
              .build()
      );
      motor.calcular(liq);
      liq.setEstado("PENDIENTE_APROBACION");
      liq = liqRepo.save(liq);
      resultado.add(toDto(liq, detalleRepo.findAllByLiquidacionId(liq.getId())));
      creadas++;
    }

    log.info("Rango diario ejecutado: {} creadas, {} omitidas (ya existían)", creadas, omitidas);
    return RangoLiquidacionDiariaResponseDto.builder()
        .fechaDesde(fechaDesde)
        .fechaHasta(fechaHasta)
        .totalDias((int) java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1)
        .creadas(creadas)
        .omitidas(omitidas)
        .liquidaciones(resultado)
        .build();
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private LiquidacionDiariaEntity findOrThrow(Long id) {
    return liqRepo.findById(id)
        .orElseThrow(() -> new CustomException("Liquidación diaria no encontrada: " + id, HttpStatus.NOT_FOUND));
  }

  private UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private LiquidacionDiariaResponseDto toDto(LiquidacionDiariaEntity liq,
                                              List<LiquidacionDiariaDetalleEntity> detalles) {
    BigDecimal retFuente = detalles.isEmpty()
        ? detalleRepo.sumRetencionFuente(liq.getId())
        : detalles.stream().map(LiquidacionDiariaDetalleEntity::getRetencionFuenteAplicada)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal retIca = detalles.isEmpty()
        ? detalleRepo.sumRetencionIca(liq.getId())
        : detalles.stream().map(LiquidacionDiariaDetalleEntity::getRetencionIcaAplicada)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal neto = liq.getTotalInteresesLiquidados().subtract(retFuente).subtract(retIca);

    String aprobadoPorNombre = null;
    if (liq.getAprobadaPor() != null) {
      aprobadoPorNombre = usuarioRepo.findById(liq.getAprobadaPor())
          .map(UsuarioEntity::getNombre).orElse(null);
    }

    List<LiquidacionDiariaDetalleItemDto> detalleDto = detalles.stream()
        .map(this::toDetalleDto)
        .collect(Collectors.toList());

    return LiquidacionDiariaResponseDto.builder()
        .id(liq.getId())
        .fecha(liq.getFecha())
        .periodo(liq.getFecha() != null ? liq.getFecha().format(FORMATO_PERIODO) : null)
        .estado(liq.getEstado())
        .totalInteresesLiquidados(liq.getTotalInteresesLiquidados())
        .totalRetencionFuente(retFuente)
        .totalRetencionIca(retIca)
        .totalNetoCobrar(neto)
        .aprobadaPorNombre(aprobadoPorNombre)
        .aprobadaAt(liq.getAprobadaAt())
        .createdAt(liq.getCreatedAt())
        .detalle(detalleDto)
        .build();
  }

  private LiquidacionDiariaDetalleItemDto toDetalleDto(LiquidacionDiariaDetalleEntity d) {
    BigDecimal neto = d.getInteresesPeriodo()
        .subtract(d.getRetencionFuenteAplicada())
        .subtract(d.getRetencionIcaAplicada());

    if (d.getSaldoInicialId() != null) {
      var si = saldoInicialRepo.findByIdAndDeletedAtIsNull(d.getSaldoInicialId()).orElse(null);
      return LiquidacionDiariaDetalleItemDto.builder()
          .id(d.getId())
          .saldoInicialId(d.getSaldoInicialId())
          .tipoFuente("SALDO_INICIAL")
          .referencia(si != null ? si.getCodigo() : null)
          .empresaPrestatariaNombre(si != null ? si.getEmpresaPrestataria().getRazonSocial() : null)
          .empresaPrestamistaNombre(si != null ? si.getEmpresaPrestamista().getRazonSocial() : null)
          .interesesPeriodo(d.getInteresesPeriodo())
          .retencionFuenteAplicada(d.getRetencionFuenteAplicada())
          .retencionIcaAplicada(d.getRetencionIcaAplicada())
          .netoCobrar(neto)
          .build();
    }

    OperacionEntity op = operacionRepo.findByIdAndDeletedAtIsNull(d.getOperacionId()).orElse(null);
    return LiquidacionDiariaDetalleItemDto.builder()
        .id(d.getId())
        .operacionId(d.getOperacionId())
        .tipoFuente("OPERACION")
        .referencia(op != null ? op.getReferencia() : null)
        .empresaPrestatariaNombre(op != null ? op.getEmpresaPrestataria().getRazonSocial() : null)
        .empresaPrestamistaNombre(op != null ? op.getEmpresaPrestamista().getRazonSocial() : null)
        .interesesPeriodo(d.getInteresesPeriodo())
        .retencionFuenteAplicada(d.getRetencionFuenteAplicada())
        .retencionIcaAplicada(d.getRetencionIcaAplicada())
        .netoCobrar(neto)
        .build();
  }
}
