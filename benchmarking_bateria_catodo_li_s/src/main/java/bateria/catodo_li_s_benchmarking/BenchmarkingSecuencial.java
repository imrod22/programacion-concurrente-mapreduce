package bateria.catodo_li_s_benchmarking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkingSecuencial {
	
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
    public static void main(String[] args) throws IOException {
    	
        String medidasPath = "data";
        
        File carpetaProcesar = new File(medidasPath);
        File[] archivosMedida = carpetaProcesar.listFiles((dir, name) -> name.endsWith(".txt"));

        if (archivosMedida == null || archivosMedida.length == 0) {
        	eventoLog("------ Procesamiento interrumpido: No se encontraron archivos .txt en la carpeta ------");
            return;
        }
        
        eventoLog("------ Iniciando procesamiento de " + archivosMedida.length + " archivos ------");
        
        for (File archivo : archivosMedida) {
            String filePath = archivo.getAbsolutePath();
            eventoLog("Procesando archivo: " + filePath);
            
            String[] fileInformacion = extraerInfoArchivo(filePath);
            String materialName = fileInformacion[0];            
            
            List<String[]> inputData = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split("\\;");
                    if (fields.length >= 5 && fields[0].trim().replace(".", "").matches("-?\\d+(\\.\\d+)?")) {
                        inputData.add(fields);
                    }
                }
            }
            
            Map<String, Double>  valoresIntermedios = faseMap(inputData, materialName);
            faseReduce(valoresIntermedios, fileInformacion);     
        
            eventoLog("Finalizado procesamiento del archivo: " + filePath);
        }
        
        eventoLog("------ Procesamiento completado. Todos los archivos han sido procesados ------");
   }
    
	private static void eventoLog(String message) {

            String tiempoFormateado = LocalDateTime.now().format(FORMATO_FECHA);
            String mensajeLog = String.format("[%s] %s%n", tiempoFormateado, message);
            
            try (FileWriter fw = new FileWriter("secuencial/log_benchmarking_material.txt", true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(mensajeLog);
            } catch (IOException e) {
                System.err.println("Error escribiendo en el log: " + e.getMessage());
            }       
    }
    
    public static String[] extraerInfoArchivo(String filePath) {
    	Path path = Paths.get(filePath);
    	
    	String fileName = path.getFileName().toString();
    	
    	int dotIndex = fileName.lastIndexOf(".");
    	
    	if(dotIndex != -1) {
    		fileName = fileName.substring(0, dotIndex);
    	}
    	
    	String[] parts = fileName.split("-");
        return parts;
    	
    }
    
    public static Map<String, Double> faseMap(List<String[]> data, String nombreMaterial) {
        Map<String, Double> medidas = new HashMap<>();
        Map<String, Integer> contadorIndices = new HashMap<>(); 
        
        for (String[] row : data) {
                
                MedidaCorriente unaMedida = new MedidaCorriente.Builder()
                		.material(nombreMaterial)
                        .tiempoStep(Double.parseDouble(row[0].trim()))
                        .step(Integer.parseInt(row[1].trim()))
                        .ciclo(Integer.parseInt(row[2].trim()))
                        .corriente(Double.parseDouble(row[3].trim()))
                        .voltaje(Double.parseDouble(row[4].trim()))
                        .build();                

                unaMedida.setCapacidadDescarga((unaMedida.getTiempoStep() * unaMedida.getCorriente()) / 3600);
                
                String claveBase = String.format("%s-%d-%d", nombreMaterial, unaMedida.getCiclo(), unaMedida.getStep()); 
                int index = contadorIndices.getOrDefault(claveBase, 0);
                contadorIndices.put(claveBase, index + 1);
                
                String claveMedida = String.format("%s-%d", claveBase, index);
                
                medidas.put(claveMedida, unaMedida.getCapacidadDescarga());
        }

        return medidas;
    }
    
    public static void faseReduce(Map<String, Double> medidas, String[] archivoCiclo) {
        Map<String, Double> maxMedidas = new HashMap<>();
        
        List<String> mass = new ArrayList<>();
        
        String outputFolder = "secuencial";
        String filePath = "";

        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        mass.add("Blanco;0.00092"); 
        mass.add("TrGOSulfurico;0.00113"); 
        mass.add("TrGOFosforico;0.00101"); 
        mass.add("GOSulfurico;0.00107"); 
        mass.add("GOFosforico;0.00088"); 
        mass.add("GOYPF;0.00096");
        
        Map<String, Double> pesoMasaMap = new HashMap<>();
        for (String item : mass) {
            String[] parts = item.split(";");
            String material = parts[0];
            double pesoMasa = Double.parseDouble(parts[1]);
            pesoMasaMap.put(material, pesoMasa);
        }
        
        for (Map.Entry<String, Double> medida : medidas.entrySet()) {
        	
        	String key = medida.getKey();
            String[] parts = key.split("-");
            
            String material = parts[0];
            int ciclo = Integer.parseInt(parts[1]);            
            int step = Integer.parseInt(parts[2]);
            
            if (step == 4) {
            	
            	String materialCicloKey = material + "-" + ciclo;
                double value =  Math.abs(medida.getValue());
                
                if (!maxMedidas.containsKey(materialCicloKey) || maxMedidas.get(materialCicloKey) < value) {
                    maxMedidas.put(materialCicloKey, value);
                }
            }
        }

        String fileName = "output_" + String.format("%s-%s", archivoCiclo[0], archivoCiclo[1]) + "_benchmarking_material.txt";
        filePath = outputFolder + File.separator + fileName; 
        
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Map.Entry<String, Double> entry : maxMedidas.entrySet()) {
                String materialCicloKey = entry.getKey();
                String material = materialCicloKey.split("-")[0];
                double valorCarga = entry.getValue();
                double pesoMasa = pesoMasaMap.get(material);

                double totalPorMasa = (valorCarga / pesoMasa) * 1000;
                    String line = materialCicloKey + " : " + totalPorMasa + "\n";
                    writer.write(line);
            }
         
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + filePath);
            e.printStackTrace();
        }
    }    
}
