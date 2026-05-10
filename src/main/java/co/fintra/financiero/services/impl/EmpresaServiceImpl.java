package co.fintra.financiero.services.impl;

import co.fintra.financiero.dto.request.empresa.*;
import co.fintra.financiero.dto.response.empresa.*;
import co.fintra.financiero.models.entity.*;
import co.fintra.financiero.models.repositories.*;
import co.fintra.financiero.services.interfaces.IEmpresaService;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaServiceImpl implements IEmpresaService {

  private final IEmpresaRepository empresaRepo;
  private final IEmpresaCuentaBancariaRepository cuentaBancariaRepo;
  private final ITasaEspecialEmpresaRepository tasaRepo;
  private final IBancoRepository bancoRepo;
  private final ICuentaContableRepository cuentaContableRepo;
  private final IUsuarioRepository usuarioRepo;

  // ──────────────────────────────────────────────────────── EMPRESAS

  @Override
  @Transactional(readOnly = true)
  public Page<EmpresaListItemDto> listar(String estado, String rolPermitido, String busqueda, Pageable pageable) {
    return empresaRepo.buscar(estado, rolPermitido, busqueda, pageable)
        .map(this::toListItem);
  }

  @Override
  @Transactional(readOnly = true)
  public EmpresaResponseDto obtener(Long id) {
    EmpresaEntity empresa = findEmpresaOrThrow(id);
    return toResponseDto(empresa);
  }

  @Override
  public EmpresaResponseDto crear(CrearEmpresaRequestDto req) {
    if (empresaRepo.existsByCodigoInterno(req.getCodigoInterno()))
      throw new BusinessException("El código interno '" + req.getCodigoInterno() + "' ya existe");
    if (empresaRepo.existsByNit(req.getNit()))
      throw new BusinessException("El NIT '" + req.getNit() + "' ya está registrado");
    validarFlagsEmpresa(req.getCobraInteres(), req.getAplicaTasaEspecial());

    EmpresaEntity empresa = EmpresaEntity.builder()
        .codigoInterno(req.getCodigoInterno())
        .razonSocial(req.getRazonSocial())
        .nit(req.getNit())
        .pais(req.getPais() != null ? req.getPais() : "Colombia")
        .ciudad(req.getCiudad())
        .rolPermitido(req.getRolPermitido())
        .estado("ACTIVA")
        .representanteLegalNombre(req.getRepresentanteLegalNombre())
        .representanteLegalEmail(req.getRepresentanteLegalEmail())
        .representanteLegalTelefono(req.getRepresentanteLegalTelefono())
        .erpUtilizado(req.getErpUtilizado())
        .centroUtilidad(req.getCentroUtilidad())
        .saldoInicialCapital(req.getSaldoInicialCapital())
        .saldoInicialIntereses(req.getSaldoInicialIntereses())
        .fechaCorteSaldoInicial(req.getFechaCorteSaldoInicial())
        .cobraInteres(Boolean.TRUE.equals(req.getCobraInteres()))
        .calculaInteresPresunto(Boolean.TRUE.equals(req.getCalculaInteresPresunto()))
        .aplicaTasaEspecial(Boolean.TRUE.equals(req.getAplicaTasaEspecial()))
        .retencionFuentePorcentaje(req.getRetencionFuentePorcentaje())
        .retencionIcaPorcentaje(req.getRetencionIcaPorcentaje())
        .build();

    if (req.getCuentaCxcId() != null)
      empresa.setCuentaCxc(cuentaContableRepo.findById(req.getCuentaCxcId())
          .orElseThrow(() -> new BusinessException("Cuenta CxC no encontrada")));
    if (req.getCuentaCxpId() != null)
      empresa.setCuentaCxp(cuentaContableRepo.findById(req.getCuentaCxpId())
          .orElseThrow(() -> new BusinessException("Cuenta CxP no encontrada")));

    return toResponseDto(empresaRepo.save(empresa));
  }

  @Override
  public EmpresaResponseDto actualizar(Long id, ActualizarEmpresaRequestDto req) {
    EmpresaEntity empresa = findEmpresaOrThrow(id);

    if (empresaRepo.existsByNitAndIdNot(req.getNit(), id))
      throw new BusinessException("El NIT '" + req.getNit() + "' ya está en uso por otra empresa");

    Boolean cobraInteres = req.getCobraInteres() != null ? req.getCobraInteres() : empresa.getCobraInteres();
    Boolean aplicaTasa   = req.getAplicaTasaEspecial() != null ? req.getAplicaTasaEspecial() : empresa.getAplicaTasaEspecial();
    validarFlagsEmpresa(cobraInteres, aplicaTasa);

    if (req.getRazonSocial()          != null) empresa.setRazonSocial(req.getRazonSocial());
    if (req.getNit()                  != null) empresa.setNit(req.getNit());
    if (req.getPais()                 != null) empresa.setPais(req.getPais());
    if (req.getCiudad()               != null) empresa.setCiudad(req.getCiudad());
    if (req.getRolPermitido()         != null) empresa.setRolPermitido(req.getRolPermitido());
    if (req.getRepresentanteLegalNombre()   != null) empresa.setRepresentanteLegalNombre(req.getRepresentanteLegalNombre());
    if (req.getRepresentanteLegalEmail()    != null) empresa.setRepresentanteLegalEmail(req.getRepresentanteLegalEmail());
    if (req.getRepresentanteLegalTelefono() != null) empresa.setRepresentanteLegalTelefono(req.getRepresentanteLegalTelefono());
    if (req.getErpUtilizado()         != null) empresa.setErpUtilizado(req.getErpUtilizado());
    if (req.getCentroUtilidad()       != null) empresa.setCentroUtilidad(req.getCentroUtilidad());
    if (req.getSaldoInicialCapital()  != null) empresa.setSaldoInicialCapital(req.getSaldoInicialCapital());
    if (req.getSaldoInicialIntereses()!= null) empresa.setSaldoInicialIntereses(req.getSaldoInicialIntereses());
    if (req.getFechaCorteSaldoInicial()!= null) empresa.setFechaCorteSaldoInicial(req.getFechaCorteSaldoInicial());
    if (req.getCobraInteres()         != null) empresa.setCobraInteres(req.getCobraInteres());
    if (req.getCalculaInteresPresunto()!= null) empresa.setCalculaInteresPresunto(req.getCalculaInteresPresunto());
    if (req.getAplicaTasaEspecial()   != null) empresa.setAplicaTasaEspecial(req.getAplicaTasaEspecial());
    if (req.getRetencionFuentePorcentaje() != null) empresa.setRetencionFuentePorcentaje(req.getRetencionFuentePorcentaje());
    if (req.getRetencionIcaPorcentaje()    != null) empresa.setRetencionIcaPorcentaje(req.getRetencionIcaPorcentaje());

    if (req.getCuentaCxcId() != null)
      empresa.setCuentaCxc(cuentaContableRepo.findById(req.getCuentaCxcId())
          .orElseThrow(() -> new BusinessException("Cuenta CxC no encontrada")));
    if (req.getCuentaCxpId() != null)
      empresa.setCuentaCxp(cuentaContableRepo.findById(req.getCuentaCxpId())
          .orElseThrow(() -> new BusinessException("Cuenta CxP no encontrada")));

    return toResponseDto(empresaRepo.save(empresa));
  }

  @Override
  public void inactivar(Long id) {
    EmpresaEntity empresa = findEmpresaOrThrow(id);
    if ("INACTIVA".equals(empresa.getEstado()))
      throw new BusinessException("La empresa ya está inactiva");
    empresa.setEstado("INACTIVA");
    empresaRepo.save(empresa);
  }

  // ──────────────────────────────────────────────────── CUENTAS BANCARIAS

  @Override
  public CuentaBancariaResponseDto agregarCuentaBancaria(Long empresaId, CuentaBancariaRequestDto req) {
    EmpresaEntity empresa = findEmpresaOrThrow(empresaId);
    BancoEntity banco = bancoRepo.findAll().stream()
        .filter(b -> b.getCodigo().equals(req.getBancoCodigo()))
        .findFirst()
        .orElseThrow(() -> new BusinessException("Banco con código '" + req.getBancoCodigo() + "' no encontrado"));

    if (cuentaBancariaRepo.existsByEmpresaIdAndBancoCodigoAndNumeroCuentaAndDeletedAtIsNull(
        empresaId, req.getBancoCodigo(), req.getNumeroCuenta()))
      throw new BusinessException("Ya existe una cuenta activa con ese banco y número");

    EmpresaCuentaBancariaEntity cuenta = EmpresaCuentaBancariaEntity.builder()
        .empresa(empresa)
        .banco(banco)
        .tipo(req.getTipo())
        .numeroCuenta(req.getNumeroCuenta())
        .titular(req.getTitular())
        .codigoContable(req.getCodigoContable())
        .formatoArchivoPlano(req.getFormatoArchivoPlano())
        .exentaGmf(Boolean.TRUE.equals(req.getExentaGmf()))
        .activa(true)
        .build();

    return toCuentaDto(cuentaBancariaRepo.save(cuenta));
  }

  @Override
  public CuentaBancariaResponseDto editarCuentaBancaria(Long empresaId, Long cuentaId, CuentaBancariaRequestDto req) {
    EmpresaCuentaBancariaEntity cuenta = cuentaBancariaRepo
        .findByIdAndEmpresaIdAndDeletedAtIsNull(cuentaId, empresaId)
        .orElseThrow(() -> new CustomException("Cuenta bancaria no encontrada", HttpStatus.NOT_FOUND));

    BancoEntity banco = bancoRepo.findAll().stream()
        .filter(b -> b.getCodigo().equals(req.getBancoCodigo()))
        .findFirst()
        .orElseThrow(() -> new BusinessException("Banco no encontrado"));

    if (cuentaBancariaRepo.existsByEmpresaIdAndBancoCodigoAndNumeroCuentaAndIdNotAndDeletedAtIsNull(
        empresaId, req.getBancoCodigo(), req.getNumeroCuenta(), cuentaId))
      throw new BusinessException("Ya existe otra cuenta activa con ese banco y número");

    cuenta.setBanco(banco);
    cuenta.setTipo(req.getTipo());
    cuenta.setNumeroCuenta(req.getNumeroCuenta());
    cuenta.setTitular(req.getTitular());
    cuenta.setCodigoContable(req.getCodigoContable());
    cuenta.setFormatoArchivoPlano(req.getFormatoArchivoPlano());
    cuenta.setExentaGmf(Boolean.TRUE.equals(req.getExentaGmf()));

    return toCuentaDto(cuentaBancariaRepo.save(cuenta));
  }

  @Override
  public void desactivarCuentaBancaria(Long empresaId, Long cuentaId) {
    EmpresaCuentaBancariaEntity cuenta = cuentaBancariaRepo
        .findByIdAndEmpresaIdAndDeletedAtIsNull(cuentaId, empresaId)
        .orElseThrow(() -> new CustomException("Cuenta bancaria no encontrada", HttpStatus.NOT_FOUND));
    cuenta.setActiva(false);
    cuentaBancariaRepo.save(cuenta);
  }

  // ─────────────────────────────────────────────────── TASAS ESPECIALES

  @Override
  public TasaEspecialResponseDto solicitarTasaEspecial(Long empresaId, SolicitarTasaEspecialRequestDto req) {
    EmpresaEntity empresa = findEmpresaOrThrow(empresaId);
    if (!Boolean.TRUE.equals(empresa.getAplicaTasaEspecial()))
      throw new BusinessException("La empresa no tiene habilitadas las tasas especiales");
    if (tasaRepo.existsByEmpresaIdAndEstadoAndDeletedAtIsNull(empresaId, "PENDIENTE"))
      throw new BusinessException("Ya existe una tasa especial pendiente de aprobación para esta empresa");
    if (req.getVigenciaHasta().isBefore(req.getVigenciaDesde()))
      throw new BusinessException("La fecha de vigencia hasta debe ser posterior a vigencia desde");

    TasaEspecialEmpresaEntity tasa = TasaEspecialEmpresaEntity.builder()
        .empresa(empresa)
        .valorPorcentajeEfectivoAnual(req.getValorPorcentajeEfectivoAnual())
        .valorPorcentajeMensual(req.getValorPorcentajeMensual())
        .vigenciaDesde(req.getVigenciaDesde())
        .vigenciaHasta(req.getVigenciaHasta())
        .estado("PENDIENTE")
        .observacion(req.getObservacion())
        .build();

    return toTasaDto(tasaRepo.save(tasa));
  }

  @Override
  public TasaEspecialResponseDto aprobarTasaEspecial(Long empresaId, Long tasaId, String observacion) {
    TasaEspecialEmpresaEntity tasa = findTasaOrThrow(tasaId, empresaId);
    if (!"PENDIENTE".equals(tasa.getEstado()))
      throw new BusinessException("Solo se puede aprobar una tasa en estado PENDIENTE");

    tasaRepo.findFirstByEmpresaIdAndEstadoAndDeletedAtIsNull(empresaId, "VIGENTE")
        .ifPresent(vigente -> {
          vigente.setEstado("VENCIDA");
          tasaRepo.save(vigente);
        });

    tasa.setEstado("VIGENTE");
    tasa.setAprobadoPor(currentUser());
    tasa.setAprobadoAt(OffsetDateTime.now());
    if (observacion != null) tasa.setObservacion(observacion);

    return toTasaDto(tasaRepo.save(tasa));
  }

  @Override
  public TasaEspecialResponseDto rechazarTasaEspecial(Long empresaId, Long tasaId, String observacion) {
    TasaEspecialEmpresaEntity tasa = findTasaOrThrow(tasaId, empresaId);
    if (!"PENDIENTE".equals(tasa.getEstado()))
      throw new BusinessException("Solo se puede rechazar una tasa en estado PENDIENTE");

    tasa.setEstado("RECHAZADA");
    tasa.setAprobadoPor(currentUser());
    tasa.setAprobadoAt(OffsetDateTime.now());
    if (observacion != null) tasa.setObservacion(observacion);

    return toTasaDto(tasaRepo.save(tasa));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TasaEspecialResponseDto> listarTasasEspeciales(Long empresaId) {
    findEmpresaOrThrow(empresaId);
    return tasaRepo.findAllByEmpresaIdAndDeletedAtIsNullOrderByVigenciaDesdeDesc(empresaId)
        .stream().map(this::toTasaDto).collect(Collectors.toList());
  }

  // ──────────────────────────────────────────────────────── HELPERS

  private EmpresaEntity findEmpresaOrThrow(Long id) {
    return empresaRepo.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CustomException("Empresa no encontrada", HttpStatus.NOT_FOUND));
  }

  private TasaEspecialEmpresaEntity findTasaOrThrow(Long tasaId, Long empresaId) {
    return tasaRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(tasaId, empresaId)
        .orElseThrow(() -> new CustomException("Tasa especial no encontrada", HttpStatus.NOT_FOUND));
  }

  private void validarFlagsEmpresa(Boolean cobraInteres, Boolean aplicaTasaEspecial) {
    if (Boolean.TRUE.equals(aplicaTasaEspecial) && !Boolean.TRUE.equals(cobraInteres))
      throw new BusinessException("No puede aplicar tasa especial si cobra_interes está deshabilitado");
  }

  private UsuarioEntity currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return null;
    return usuarioRepo.findByUsernameAndDeletedAtIsNull(auth.getName()).orElse(null);
  }

  private EmpresaListItemDto toListItem(EmpresaEntity e) {
    boolean tienePendiente = tasaRepo.existsByEmpresaIdAndEstadoAndDeletedAtIsNull(e.getId(), "PENDIENTE");
    return EmpresaListItemDto.builder()
        .id(e.getId())
        .codigoInterno(e.getCodigoInterno())
        .razonSocial(e.getRazonSocial())
        .nit(e.getNit())
        .rolPermitido(e.getRolPermitido())
        .estado(e.getEstado())
        .erpUtilizado(e.getErpUtilizado())
        .cobraInteres(e.getCobraInteres())
        .aplicaTasaEspecial(e.getAplicaTasaEspecial())
        .tieneTasaPendiente(tienePendiente)
        .build();
  }

  private EmpresaResponseDto toResponseDto(EmpresaEntity e) {
    List<CuentaBancariaResponseDto> cuentas = cuentaBancariaRepo
        .findAllByEmpresaIdAndDeletedAtIsNullOrderByIdAsc(e.getId())
        .stream().map(this::toCuentaDto).collect(Collectors.toList());
    List<TasaEspecialResponseDto> tasas = tasaRepo
        .findAllByEmpresaIdAndDeletedAtIsNullOrderByVigenciaDesdeDesc(e.getId())
        .stream().map(this::toTasaDto).collect(Collectors.toList());

    return EmpresaResponseDto.builder()
        .id(e.getId())
        .codigoInterno(e.getCodigoInterno())
        .razonSocial(e.getRazonSocial())
        .nit(e.getNit())
        .pais(e.getPais())
        .ciudad(e.getCiudad())
        .rolPermitido(e.getRolPermitido())
        .estado(e.getEstado())
        .representanteLegalNombre(e.getRepresentanteLegalNombre())
        .representanteLegalEmail(e.getRepresentanteLegalEmail())
        .representanteLegalTelefono(e.getRepresentanteLegalTelefono())
        .erpUtilizado(e.getErpUtilizado())
        .cuentaCxcId(e.getCuentaCxc() != null ? e.getCuentaCxc().getId() : null)
        .cuentaCxcCodigo(e.getCuentaCxc() != null ? e.getCuentaCxc().getCodigo() : null)
        .cuentaCxpId(e.getCuentaCxp() != null ? e.getCuentaCxp().getId() : null)
        .cuentaCxpCodigo(e.getCuentaCxp() != null ? e.getCuentaCxp().getCodigo() : null)
        .centroUtilidad(e.getCentroUtilidad())
        .saldoInicialCapital(e.getSaldoInicialCapital())
        .saldoInicialIntereses(e.getSaldoInicialIntereses())
        .fechaCorteSaldoInicial(e.getFechaCorteSaldoInicial())
        .cobraInteres(e.getCobraInteres())
        .calculaInteresPresunto(e.getCalculaInteresPresunto())
        .aplicaTasaEspecial(e.getAplicaTasaEspecial())
        .retencionFuentePorcentaje(e.getRetencionFuentePorcentaje())
        .retencionIcaPorcentaje(e.getRetencionIcaPorcentaje())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .cuentasBancarias(cuentas)
        .tasasEspeciales(tasas)
        .build();
  }

  private CuentaBancariaResponseDto toCuentaDto(EmpresaCuentaBancariaEntity c) {
    return CuentaBancariaResponseDto.builder()
        .id(c.getId())
        .bancoCodigo(c.getBanco().getCodigo())
        .bancoNombre(c.getBanco().getNombre())
        .tipo(c.getTipo())
        .numeroCuenta(c.getNumeroCuenta())
        .titular(c.getTitular())
        .codigoContable(c.getCodigoContable())
        .formatoArchivoPlano(c.getFormatoArchivoPlano())
        .exentaGmf(c.getExentaGmf())
        .activa(c.getActiva())
        .build();
  }

  private TasaEspecialResponseDto toTasaDto(TasaEspecialEmpresaEntity t) {
    return TasaEspecialResponseDto.builder()
        .id(t.getId())
        .valorPorcentajeEfectivoAnual(t.getValorPorcentajeEfectivoAnual())
        .valorPorcentajeMensual(t.getValorPorcentajeMensual())
        .vigenciaDesde(t.getVigenciaDesde())
        .vigenciaHasta(t.getVigenciaHasta())
        .estado(t.getEstado())
        .aprobadoPorNombre(t.getAprobadoPor() != null ? t.getAprobadoPor().getNombre() : null)
        .aprobadoAt(t.getAprobadoAt())
        .observacion(t.getObservacion())
        .createdAt(t.getCreatedAt())
        .build();
  }
}
