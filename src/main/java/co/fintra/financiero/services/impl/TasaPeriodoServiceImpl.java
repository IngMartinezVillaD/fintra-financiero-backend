package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.request.tasas.RegistrarTasaRequestDto;
import co.fintra.financiero.dto.response.tasas.EstadoBloqueoDto;
import co.fintra.financiero.dto.response.tasas.TasaPeriodoResponseDto;
import co.fintra.financiero.models.entity.TasaPeriodoEntity;
import co.fintra.financiero.models.entity.UsuarioEntity;
import co.fintra.financiero.models.repositories.IEmpresaRepository;
import co.fintra.financiero.models.repositories.ITasaEspecialEmpresaRepository;
import co.fintra.financiero.models.repositories.ITasaPeriodoRepository;
import co.fintra.financiero.models.repositories.IUsuarioRepository;
import co.fintra.financiero.services.interfaces.ITasaPeriodoService;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import co.fintra.financiero.utils.valueobject.Porcentaje;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TasaPeriodoServiceImpl implements ITasaPeriodoService {

  private static final Set<String> TIPOS_REQUERIDOS =
      Set.of("USURA", "COMERCIAL_VIGENTE", "PRESUNTA_FISCAL");

  private final ITasaPeriodoRepository tasaRepo;
  private final IUsuarioRepository usuarioRepo;
  private final IEmpresaRepository empresaRepo;
  private final ITasaEspecialEmpresaRepository tasaEspecialRepo;

  @Override
  @Transactional(readOnly = true)
  public List<TasaPeriodoResponseDto> listar() {
    return tasaRepo.findAllByDeletedAtIsNullOrderByAnioDescMesDescTipoTasaAsc()
        .stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TasaPeriodoResponseDto> listarPendientes() {
    return tasaRepo.findPendientes().stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<TasaPeriodoResponseDto> listarVigentesHoy() {
    return tasaRepo.findVigentesEnFecha(LocalDate.now())
        .stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  public TasaPeriodoResponseDto registrar(RegistrarTasaRequestDto req) {
    if (req.getVigenciaHasta().isBefore(req.getVigenciaDesde()))
      throw new BusinessException("La vigencia hasta debe ser posterior a vigencia desde");

    // Verificar EA y EM consistentes (tolerancia ±0.05%)
    var emCalculada = Porcentaje.eaToEm(req.getValorPorcentajeEfectivoAnual());
    var diferencia = req.getValorPorcentajeMensual().subtract(emCalculada).abs();
    if (diferencia.doubleValue() > 0.05)
      throw new BusinessException(
          String.format("La tasa mensual %.4f%% no es consistente con EA %.4f%% (esperado ~%.4f%%)",
              req.getValorPorcentajeMensual(), req.getValorPorcentajeEfectivoAnual(), emCalculada));

    var existente = tasaRepo.findByAnioAndMesAndTipoTasaAndDeletedAtIsNull(
        req.getAnio(), req.getMes(), req.getTipoTasa());

    if (existente.isPresent()) {
      TasaPeriodoEntity t = existente.get();
      if ("PENDIENTE".equals(t.getEstado()))
        throw new BusinessException("Ya existe una tasa pendiente de aprobación para " +
            req.getTipoTasa() + " en " + req.getAnio() + "-" + String.format("%02d", req.getMes()));
      if ("APROBADA".equals(t.getEstado()))
        throw new BusinessException("Ya existe una tasa aprobada para " +
            req.getTipoTasa() + " en " + req.getAnio() + "-" + String.format("%02d", req.getMes()));

      // RECHAZADA → re-enviar actualizando el registro
      t.setValorPorcentajeEfectivoAnual(req.getValorPorcentajeEfectivoAnual());
      t.setValorPorcentajeMensual(req.getValorPorcentajeMensual());
      t.setVigenciaDesde(req.getVigenciaDesde());
      t.setVigenciaHasta(req.getVigenciaHasta());
      t.setEstado("PENDIENTE");
      t.setAprobadoPor(null);
      t.setAprobadoAt(null);
      t.setObservacionAprobacion(req.getObservacion());
      return toDto(tasaRepo.save(t));
    }

    TasaPeriodoEntity nueva = TasaPeriodoEntity.builder()
        .anio(req.getAnio())
        .mes(req.getMes())
        .tipoTasa(req.getTipoTasa())
        .valorPorcentajeEfectivoAnual(req.getValorPorcentajeEfectivoAnual())
        .valorPorcentajeMensual(req.getValorPorcentajeMensual())
        .vigenciaDesde(req.getVigenciaDesde())
        .vigenciaHasta(req.getVigenciaHasta())
        .estado("PENDIENTE")
        .observacionAprobacion(req.getObservacion())
        .build();

    return toDto(tasaRepo.save(nueva));
  }

  @Override
  public TasaPeriodoResponseDto aprobar(Long id, String observacion) {
    TasaPeriodoEntity tasa = findOrThrow(id);
    if (!"PENDIENTE".equals(tasa.getEstado()))
      throw new BusinessException("Solo se puede aprobar una tasa en estado PENDIENTE");

    tasa.setEstado("APROBADA");
    tasa.setAprobadoPor(currentUser());
    tasa.setAprobadoAt(OffsetDateTime.now());
    if (observacion != null) tasa.setObservacionAprobacion(observacion);

    return toDto(tasaRepo.save(tasa));
  }

  @Override
  public TasaPeriodoResponseDto rechazar(Long id, String motivo) {
    TasaPeriodoEntity tasa = findOrThrow(id);
    if (!"PENDIENTE".equals(tasa.getEstado()))
      throw new BusinessException("Solo se puede rechazar una tasa en estado PENDIENTE");
    if (motivo == null || motivo.isBlank())
      throw new BusinessException("El motivo de rechazo es obligatorio");

    tasa.setEstado("RECHAZADA");
    tasa.setAprobadoPor(currentUser());
    tasa.setAprobadoAt(OffsetDateTime.now());
    tasa.setObservacionAprobacion(motivo);

    return toDto(tasaRepo.save(tasa));
  }

  @Override
  @Transactional(readOnly = true)
  public EstadoBloqueoDto evaluarBloqueoSistema() {
    LocalDate hoy = LocalDate.now();
    var vigentes = tasaRepo.findVigentesEnFecha(hoy);
    var tiposVigentes = vigentes.stream()
        .map(TasaPeriodoEntity::getTipoTasa)
        .collect(Collectors.toSet());

    for (String tipo : TIPOS_REQUERIDOS) {
      if (!tiposVigentes.contains(tipo)) {
        return EstadoBloqueoDto.builder()
            .estado("BLOQUEADO_GLOBAL")
            .motivo("No hay tasa " + tipoLabel(tipo) + " vigente para la fecha actual (" + hoy + ")")
            .tasaTipo(tipo)
            .ruta("/configuracion/tasas-periodo")
            .rutaLabel("Registrar tasas del período")
            .build();
      }
    }

    return EstadoBloqueoDto.builder().estado("OPERATIVO").build();
  }

  @Override
  @Transactional(readOnly = true)
  public EstadoBloqueoDto evaluarBloqueoEmpresa(Long empresaId) {
    var empresa = empresaRepo.findByIdAndDeletedAtIsNull(empresaId)
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));

    if (!Boolean.TRUE.equals(empresa.getAplicaTasaEspecial()))
      return EstadoBloqueoDto.builder().estado("OPERATIVO").build();

    boolean tieneVigente = tasaEspecialRepo
        .existsByEmpresaIdAndEstadoAndDeletedAtIsNull(empresaId, "VIGENTE");

    if (!tieneVigente) {
      boolean tienePendiente = tasaEspecialRepo
          .existsByEmpresaIdAndEstadoAndDeletedAtIsNull(empresaId, "PENDIENTE");
      return EstadoBloqueoDto.builder()
          .estado("BLOQUEADO_EMPRESA")
          .motivo(tienePendiente
              ? "La empresa tiene tasa especial pendiente de aprobación"
              : "La empresa requiere tasa especial vigente para operar")
          .ruta("/configuracion/empresas/" + empresaId)
          .rutaLabel("Ver empresa")
          .build();
    }

    return EstadoBloqueoDto.builder().estado("OPERATIVO").build();
  }

  // ─────────────────────────────────────── helpers

  private TasaPeriodoEntity findOrThrow(Long id) {
    return tasaRepo.findById(id)
        .orElseThrow(() -> new CustomException("Tasa de período no encontrada", HttpStatus.NOT_FOUND));
  }

  private UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private String tipoLabel(String tipo) {
    return switch (tipo) {
      case "USURA"             -> "USURA (máximo Superfinanciera)";
      case "COMERCIAL_VIGENTE" -> "COMERCIAL VIGENTE";
      case "PRESUNTA_FISCAL"   -> "PRESUNTA FISCAL";
      default                  -> tipo;
    };
  }

  private TasaPeriodoResponseDto toDto(TasaPeriodoEntity t) {
    return TasaPeriodoResponseDto.builder()
        .id(t.getId())
        .anio(t.getAnio())
        .mes(t.getMes())
        .tipoTasa(t.getTipoTasa())
        .valorPorcentajeEfectivoAnual(t.getValorPorcentajeEfectivoAnual())
        .valorPorcentajeMensual(t.getValorPorcentajeMensual())
        .vigenciaDesde(t.getVigenciaDesde())
        .vigenciaHasta(t.getVigenciaHasta())
        .estado(t.getEstado())
        .aprobadoPorNombre(t.getAprobadoPor() != null ? t.getAprobadoPor().getNombre() : null)
        .aprobadoAt(t.getAprobadoAt())
        .observacionAprobacion(t.getObservacionAprobacion())
        .createdAt(t.getCreatedAt())
        .build();
  }
}
