package dataprocessing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

public class obtencionmedidas {
    public static void main(String[] args) {
        String sourceFolder = "source";
        String dataFolder = "data";

        File outputDir = new File(dataFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File folder = new File(sourceFolder);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xls"));
        
        if (files == null || files.length == 0) {
            System.out.println("No se encontraron archivos .xls en la carpeta.");
            return;
        }
        
        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file);
					Workbook workbook = new HSSFWorkbook(fis)) {
            	String fileName = file.getName();
                
                Integer fileNumber = extractFileNumber(fileName);

                
                String txtFileName = fileName.replaceAll("\\.xls$", ".txt").replaceAll("\\.xlsx$", ".txt");
                String txtFilePath = dataFolder + File.separator + txtFileName;
                
                try (FileWriter writer = new FileWriter(txtFilePath)) {
                	
                	for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                        Sheet sheet = workbook.getSheetAt(sheetIndex);
                        
                        for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                            Row row = sheet.getRow(rowIndex);
                            if (row == null) continue;

                            StringBuilder line = new StringBuilder();

                            Cell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            BigDecimal value1 = getCellValueAsBigDecimal(cell1);

                            Cell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Integer value2 = getCellValueAsInteger(cell2);

                            Cell cell3 = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            Integer value3Original = getCellValueAsInteger(cell3);
                            Integer value3Sumado = fileNumber + value3Original;

                            Cell cell4 = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            BigDecimal value4 = getCellValueAsBigDecimal(cell4);

                            Cell cell5 = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            BigDecimal value5 = getCellValueAsBigDecimal(cell5);

                            line.append(formatBigDecimal(value1)).append("; ")
                                .append(value2).append("; ")
                                .append(value3Sumado).append("; ")
                                .append(formatBigDecimal(value4)).append("; ")
                                .append(formatBigDecimal(value5));

                            writer.write(line.toString());
                            writer.write(System.lineSeparator());
                        }
                	
                	}
                	
                 
                }

                System.out.println("Archivo generado exitosamente: " + txtFilePath);

            } catch (IOException e) {
                System.err.println("Error al procesar el archivo: " + file.getName());
                e.printStackTrace();
            }
        }

        System.out.println("Proceso completado. Todos los archivos han sido generados.");
    }
    
    private static Integer extractFileNumber(String fileName) {
        String numberPart = fileName.replaceAll("[^0-9]", "");
        return numberPart.isEmpty() ? 0 : Integer.parseInt(numberPart);
    }

    private static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        
        try {
            return new BigDecimal(cell.getStringCellValue().trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    private static int getCellValueAsInteger(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) Math.round(cell.getNumericCellValue());
        }
        try {
            return Integer.parseInt(cell.getStringCellValue().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatBigDecimal(BigDecimal value) {
        return value.toPlainString();
    }
}