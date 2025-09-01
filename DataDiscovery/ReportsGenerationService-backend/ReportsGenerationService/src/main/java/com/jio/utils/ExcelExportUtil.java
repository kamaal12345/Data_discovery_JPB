package com.jio.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.jio.entity.PiiScanResult;

import java.io.OutputStream;
import java.util.List;

public class ExcelExportUtil {

    public static void exportPiiResultsToExcel(List<PiiScanResult> results, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PII Scan Results");

        // Create bold font for headers
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12); // Optional: increase font size
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        // Header row with bold style
        Row header = sheet.createRow(0);

        String[] headers = { "File Path", "PII Types", "Matched Data", "Host/IP" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (PiiScanResult result : results) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(result.getFilePath());
            row.createCell(1).setCellValue(result.getPiiType());
            row.createCell(2).setCellValue(result.getMatchedData());
            row.createCell(3).setCellValue(result.getIp());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }
}
