package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.request.operaciones.CrearOperacionRequestDto;
import co.fintra.financiero.dto.response.operaciones.AvisoTramoAnteriorDto;
import co.fintra.financiero.dto.response.operaciones.OperacionListItemDto;
import co.fintra.financiero.dto.response.operaciones.OperacionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IOperacionService {

  Page<OperacionListItemDto> listar(String estado, Long prestamistaId, Long prestatariaId,
                                    String referencia, Pageable pageable);

  OperacionResponseDto obtener(Long id);

  OperacionResponseDto crear(CrearOperacionRequestDto request);

  OperacionResponseDto editar(Long id, CrearOperacionRequestDto request);

  OperacionResponseDto cancelar(Long id, String motivo);

  OperacionResponseDto enviarAprobacion(Long id);

  Optional<AvisoTramoAnteriorDto> calcularAvisoTramoAnterior(Long empresaPrestatariaId);

  // ── AI: Aprobación interna ──────────────────────────
  OperacionResponseDto aprobarInterna(Long id, String observacion);
  OperacionResponseDto devolverDesdeAI(Long id, String observacion);
  OperacionResponseDto rechazarInterna(Long id, String motivo);

  // ── AE: Aceptación empresa ──────────────────────────
  OperacionResponseDto aceptarEmpresa(Long id, String observacion);
  OperacionResponseDto rechazarEmpresa(Long id, String motivo);

  // ── Bandejas ─────────────────────────────────────────
  java.util.List<OperacionListItemDto> listarPendientesAprobacion();
  java.util.List<OperacionListItemDto> listarPendientesAceptacion();

  // ── Historial ────────────────────────────────────────
  java.util.List<co.fintra.financiero.dto.response.operaciones.EventoPipelineDto> historial(Long id);
}
