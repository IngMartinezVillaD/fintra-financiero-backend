package co.pluto.services.impl;

import co.pluto.models.entity.*;
import co.pluto.models.repositories.*;
import co.pluto.services.interfaces.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportesServiceImpl {

  private final IOperacionRepository            operacionRepo;
  private final IDesembolsoRepository           desembolsoRepo;
  private final IAbonoRepository                abonoRepo;
  private final ITramoRepository                tramoRepo;
  private final IGmfMovimientoRepository        gmfRepo;
  private final IInteresPresuntoRepository      presuntoRepo;
  private final ILiquidacionMensualRepository   liqRepo;
  private final ILiquidacionMensualDetalleRepository detalleRepo;
  private final IEmpresaRepository              empresaRepo;
  private final IDashboardService               dashboardService;

  // ── Reporte de saldos a fecha ────────────────────────────────────

  public byte[] reporteSaldos(LocalDate fecha) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("Saldos " + fecha);
      CellStyle header = headerStyle(wb);

      Row h = sheet.createRow(0);
      String[] cols = {"Empresa", "NIT", "Operaciones DS", "Saldo Capital", "Intereses Causados",
                        "Total Desembolsado", "Último Desembolso"};
      for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

      int row = 1;
      for (var c : dashboardService.consolidadoFinanciero()) {
        Row r = sheet.createRow(row++);
        cell(r, 0, c.getRazonSocial());
        cell(r, 1, c.getNit());
        cell(r, 2, String.valueOf(c.getTotalOperaciones()));
        cell(r, 3, fmt(c.getSaldoCapitalVigente()));
        cell(r, 4, fmt(c.getInteresesCausadosPendientes()));
        cell(r, 5, fmt(c.getTotalDesembolsado()));
        cell(r, 6, c.getUltimoDesembolsoFecha() != null ? c.getUltimoDesembolsoFecha().toString() : "");
      }

      autoSize(sheet, cols.length);
      return toBytes(wb);
    }
  }

  // ── Reporte liquidación anual ────────────────────────────────────

  public byte[] reporteLiquidacionAnual(Short anio) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("Liquidación " + anio);
      CellStyle header = headerStyle(wb);

      Row h = sheet.createRow(0);
      String[] cols = {"Mes", "Estado", "Total Intereses", "Ret. Fuente", "Ret. ICA", "Neto"};
      for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

      int row = 1;
      for (var liq : liqRepo.findAllByOrderByAnioDescMesDesc()) {
        if (!anio.equals(liq.getAnio())) continue;
        Row r = sheet.createRow(row++);
        cell(r, 0, mesNombre(liq.getMes()) + " " + liq.getAnio());
        cell(r, 1, liq.getEstado());
        cell(r, 2, fmt(liq.getTotalInteresesLiquidados()));
        cell(r, 3, fmt(detalleRepo.sumRetencionFuente(liq.getId())));
        cell(r, 4, fmt(detalleRepo.sumRetencionIca(liq.getId())));
        BigDecimal neto = liq.getTotalInteresesLiquidados()
            .subtract(detalleRepo.sumRetencionFuente(liq.getId()))
            .subtract(detalleRepo.sumRetencionIca(liq.getId()));
        cell(r, 5, fmt(neto));
      }

      autoSize(sheet, cols.length);
      return toBytes(wb);
    }
  }

  // ── Reporte auditoría pipeline ───────────────────────────────────

  public byte[] reporteAuditoriaPipeline(LocalDate desde, LocalDate hasta) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("Auditoría Pipeline");
      CellStyle header = headerStyle(wb);

      Row h = sheet.createRow(0);
      String[] cols = {"Referencia", "Estado Anterior", "Estado Nuevo", "Usuario", "Observación", "Fecha"};
      for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

      // Listar todas las operaciones con tramos y eventos del período
      int row = 1;
      for (OperacionEntity op : operacionRepo.findAllByEstadoPipelineAndDeletedAtIsNullOrderByCreatedAtAsc("DS")) {
        for (TramoEntity tramo : tramoRepo.findAllByOperacionIdAndDeletedAtIsNullOrderByFechaDesdeAsc(op.getId())) {
          if (tramo.getFechaDesde().isBefore(desde) || tramo.getFechaDesde().isAfter(hasta)) continue;
          Row r = sheet.createRow(row++);
          cell(r, 0, op.getReferencia());
          cell(r, 1, tramo.getTipoMovimiento());
          cell(r, 2, tramo.getEstado());
          cell(r, 3, tramo.getCreatedBy());
          cell(r, 4, "Saldo: " + fmt(tramo.getSaldoCapital()));
          cell(r, 5, tramo.getFechaDesde().toString());
        }
      }

      autoSize(sheet, cols.length);
      return toBytes(wb);
    }
  }

  // ── Reporte GMF anual ────────────────────────────────────────────

  public byte[] reporteGmfAnual(Short anio) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("GMF " + anio);
      CellStyle header = headerStyle(wb);

      Row h = sheet.createRow(0);
      String[] cols = {"Empresa", "Mes", "Operación", "Monto GMF", "Decisión"};
      for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

      int row = 1;
      for (var mov : gmfRepo.findAllByAnioOrderByMesAsc(anio)) {
        String empresa = empresaRepo.findByIdAndDeletedAtIsNull(mov.getEmpresaId())
            .map(EmpresaEntity::getRazonSocial).orElse("" + mov.getEmpresaId());
        Row r = sheet.createRow(row++);
        cell(r, 0, empresa);
        cell(r, 1, mesNombre(mov.getMes()));
        cell(r, 2, "Op. " + mov.getOperacionId());
        cell(r, 3, fmt(mov.getMontoGmf()));
        cell(r, 4, mov.getDecisionAnual());
      }

      autoSize(sheet, cols.length);
      return toBytes(wb);
    }
  }

  // ── Reporte presunto DIAN ────────────────────────────────────────

  public byte[] reportePresuntoDian(Short anio) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("Presunto DIAN " + anio);
      CellStyle header = headerStyle(wb);

      Row h = sheet.createRow(0);
      String[] cols = {"Empresa", "Mes", "Operación", "Saldo Promedio",
                        "Tasa Preferencial %", "Días", "Monto Calculado"};
      for (int i = 0; i < cols.length; i++) cell(h, i, cols[i], header);

      int row = 1;
      for (short mes = 1; mes <= 12; mes++) {
        for (var mov : presuntoRepo.findAllByAnioAndMes(anio, mes)) {
          String empresa = empresaRepo.findByIdAndDeletedAtIsNull(mov.getEmpresaId())
              .map(EmpresaEntity::getRazonSocial).orElse("" + mov.getEmpresaId());
          Row r = sheet.createRow(row++);
          cell(r, 0, empresa);
          cell(r, 1, mesNombre(mes));
          cell(r, 2, "Op. " + mov.getOperacionId());
          cell(r, 3, fmt(mov.getSaldoCapitalPromedio()));
          cell(r, 4, mov.getTasaPresuntaPorcentaje().toPlainString());
          cell(r, 5, String.valueOf(mov.getDias()));
          cell(r, 6, fmt(mov.getMontoCalculado()));
        }
      }

      autoSize(sheet, cols.length);
      return toBytes(wb);
    }
  }

  // ── helpers Excel ─────────────────────────────────────────────────

  private CellStyle headerStyle(Workbook wb) {
    CellStyle cs = wb.createCellStyle();
    Font f = wb.createFont();
    f.setBold(true);
    cs.setFont(f);
    cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return cs;
  }

  private void cell(Row r, int col, String val) {
    r.createCell(col).setCellValue(val != null ? val : "");
  }

  private void cell(Row r, int col, String val, CellStyle cs) {
    Cell c = r.createCell(col);
    c.setCellValue(val != null ? val : "");
    c.setCellStyle(cs);
  }

  private void autoSize(Sheet sheet, int cols) {
    for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
  }

  private byte[] toBytes(XSSFWorkbook wb) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    wb.write(out);
    return out.toByteArray();
  }

  private String fmt(BigDecimal v) {
    return v != null ? v.setScale(2, java.math.RoundingMode.HALF_EVEN).toPlainString() : "0.00";
  }

  private String mesNombre(short mes) {
    String[] m = {"","ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"};
    return mes >= 1 && mes <= 12 ? m[mes] : String.valueOf(mes);
  }
}
