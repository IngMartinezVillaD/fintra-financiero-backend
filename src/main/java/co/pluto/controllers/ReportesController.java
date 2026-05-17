package co.pluto.controllers;

import co.pluto.services.impl.ReportesServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Exportación Excel de saldos, liquidaciones, GMF y presunto DIAN")
public class ReportesController {

  private final ReportesServiceImpl reportesService;

  private static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  @GetMapping("/saldos")
  @Operation(summary = "Reporte de saldos por empresa a una fecha")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> saldos(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
      throws IOException {
    LocalDate f = fecha != null ? fecha : LocalDate.now();
    byte[] bytes = reportesService.reporteSaldos(f);
    return excel(bytes, "saldos-" + f + ".xlsx");
  }

  @GetMapping("/liquidacion-anual/{anio}")
  @Operation(summary = "Reporte anual de liquidaciones mensuales")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> liquidacionAnual(@PathVariable Short anio) throws IOException {
    return excel(reportesService.reporteLiquidacionAnual(anio), "liquidacion-" + anio + ".xlsx");
  }

  @GetMapping("/auditoria-pipeline")
  @Operation(summary = "Bitácora de tramos por rango de fechas")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> auditoriaPipeline(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) throws IOException {
    return excel(reportesService.reporteAuditoriaPipeline(desde, hasta),
        "auditoria-" + desde + "-" + hasta + ".xlsx");
  }

  @GetMapping("/gmf/{anio}")
  @Operation(summary = "Reporte GMF anual por empresa")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> gmf(@PathVariable Short anio) throws IOException {
    return excel(reportesService.reporteGmfAnual(anio), "gmf-" + anio + ".xlsx");
  }

  @GetMapping("/presunto/{anio}")
  @Operation(summary = "Reporte interés presunto anual para depuración DIAN")
  @PreAuthorize("hasAnyAuthority('ADMIN','TESORERIA','CONTABILIDAD')")
  public ResponseEntity<byte[]> presunto(@PathVariable Short anio) throws IOException {
    return excel(reportesService.reportePresuntoDian(anio), "presunto-dian-" + anio + ".xlsx");
  }

  private ResponseEntity<byte[]> excel(byte[] bytes, String filename) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType(EXCEL_MIME))
        .body(bytes);
  }
}
