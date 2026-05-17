package co.pluto.services.impl.seguimiento;

import co.pluto.models.entity.OperacionEntity;
import co.pluto.models.entity.TasaEspecialEmpresaEntity;
import co.pluto.models.repositories.ITasaEspecialEmpresaRepository;
import co.pluto.models.repositories.ITasaPeriodoRepository;
import co.pluto.utils.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ResolutorTasaAplicableService {

  private final ITasaPeriodoRepository    tasaPeriodoRepo;
  private final ITasaEspecialEmpresaRepository tasaEspecialRepo;

  public TasaAplicable resolver(OperacionEntity op, LocalDate fecha) {
    return switch (op.getCobraInteres()) {
      case "NO" -> TasaAplicable.sinInteres();
      case "SI_COMERCIAL" -> resolverComercial(fecha);
      case "SI_ESPECIAL"  -> resolverEspecial(op.getEmpresaPrestataria().getId(), fecha);
      default -> throw new BusinessException("Tipo de interés desconocido: " + op.getCobraInteres());
    };
  }

  private TasaAplicable resolverComercial(LocalDate fecha) {
    return tasaPeriodoRepo.findVigentesEnFecha(fecha).stream()
        .filter(t -> "COMERCIAL_VIGENTE".equals(t.getTipoTasa()))
        .findFirst()
        .map(t -> new TasaAplicable(t.getValorPorcentajeMensual(), "COMERCIAL"))
        .orElseThrow(() -> new BusinessException(
            "No existe tasa COMERCIAL_VIGENTE para la fecha " + fecha));
  }

  private TasaAplicable resolverEspecial(Long empresaId, LocalDate fecha) {
    return tasaEspecialRepo
        .findAllByEmpresaIdAndDeletedAtIsNullOrderByVigenciaDesdeDesc(empresaId)
        .stream()
        .filter(t -> "VIGENTE".equals(t.getEstado()))
        .filter(t -> !fecha.isBefore(t.getVigenciaDesde()) && !fecha.isAfter(t.getVigenciaHasta()))
        .findFirst()
        .map(t -> new TasaAplicable(t.getValorPorcentajeMensual(), "ESPECIAL"))
        .orElseThrow(() -> new BusinessException(
            "No existe tasa especial VIGENTE para la empresa en la fecha " + fecha));
  }
}
