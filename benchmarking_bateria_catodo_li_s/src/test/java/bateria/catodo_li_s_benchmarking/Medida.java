package bateria.catodo_li_s_benchmarking;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class Medida {
	 public static void main(String[] args) throws IOException {
		 String folderPath = "source";

		 String outputPath = "data";
		 
	        File outputDir = new File(outputPath);
	        if (!outputDir.exists()) {
	            outputDir.mkdirs();
	        }
	        
	        File folder = new File(folderPath);
	        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xls"));

	        if (files == null || files.length == 0) {
	            System.out.println("No se encontraron archivos .xlsx en la carpeta.");
	            return;
	        }
	        
	        for (File file : files) {

		        try (FileInputStream fis = new FileInputStream(file);
		             Workbook workbook = new HSSFWorkbook(fis)) {
		        	
	                String fileName = file.getName();
	                int fileNumber = extractFileNumber(fileName);
		        	
		        	Sheet sheet = workbook.getSheetAt(0);
		            String txtFileName = file.getName().replace(".xls", ".txt");
		            String txtFilePath = outputPath + File.separator + txtFileName;
		            
		            try (FileWriter writer = new FileWriter(txtFilePath)) {
	                    for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	                    	Row row = sheet.getRow(rowIndex);
	                        if (row == null) continue;

	                        StringBuilder line = new StringBuilder();

	                        Cell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                        BigDecimal value1 = getCellValueAsBigDecimal(cell1);

	                        Cell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                        Integer value2 = getCellValueAsInteger(cell2);

	                        Cell cell3 = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                        Integer value3 = getCellValueAsInteger(cell3);

	                        Cell cell4 = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                        BigDecimal value4Original = getCellValueAsBigDecimal(cell4);
	                        BigDecimal value4Sumado = BigDecimal.valueOf(fileNumber).add(value4Original);	                        
	                        
	                        Cell cell5 = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                        BigDecimal value5 = getCellValueAsBigDecimal(cell5);

	                        line.append(formatBigDecimal(value1)).append("; ")
                            .append(formatInteger(value2)).append("; ")
                            .append(formatInteger(value3)).append("; ")
                            .append(formatBigDecimal(value4Sumado)).append("; ")
                            .append(formatBigDecimal(value5));

	                        writer.write(line.toString());
	                        writer.write(System.lineSeparator());
	                    }
	                } 
		            
		            System.out.println("Archivo generado: " + txtFilePath);

	            } catch (IOException e) {
	                System.err.println("Error al procesar el archivo: " + file.getName());
	                e.printStackTrace();
	            }
		       }
	        }
	        
	        private static int extractFileNumber(String fileName) {
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
	        	 String plainString = value.toPlainString();
	             return plainString.isEmpty() ? "0" : plainString;
	        }
	        
	        private static String formatInteger(Integer value) {
	            if (value == null) {
	                return "0";
	            }
	                return String.valueOf(value);
	        }
}
	
