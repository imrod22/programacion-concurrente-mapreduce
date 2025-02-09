package bateria.catodo_li_s_benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BenchmarkingConcurrente {
	 public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
	        String medidasPath = "data";

	        File folder = new File(medidasPath);
	        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
	        if (files == null || files.length == 0) {
	            System.out.println("No se encontraron archivos .txt en la carpeta.");
	            return;
	        }

	        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	        List<Future<Void>> futures = new ArrayList<>();
	        
	        for (File file : files) {
	            Callable<Void> task = () -> {
	                String filePath = file.getAbsolutePath();
	                System.out.println("Procesando archivo: " + filePath);

	                String[] fileInformacion = extractBaseName(filePath);

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

	                Map<String, Double> valoresIntermedios = faseMap(inputData, fileInformacion);	                
	                faseReduce(valoresIntermedios, fileInformacion);
					return null;
	            };

	            futures.add(executor.submit(task));
	        }

	        for (Future<Void> future : futures) {
	            future.get();
	        }

	        executor.shutdown();
	        executor.awaitTermination(1, TimeUnit.MINUTES);

	        System.out.println("Proceso completado. Todos los archivos han sido procesados.");
	 }
	 
	    public static String[] extractBaseName(String filePath) {
	        Path path = Paths.get(filePath);
	        String fileName = path.getFileName().toString();
	        int dotIndex = fileName.lastIndexOf(".");
	        if (dotIndex != -1) {
	            fileName = fileName.substring(0, dotIndex);
	        }
	        return fileName.split("-");
	    }
	    
	    public static Map<String, Double> faseMap(List<String[]> data, String[] archivoCiclo) {
	        Map<String, Double> medidas = new HashMap<>();
	        Map<String, Integer> contadorIndices = new HashMap<>();

	        for (String[] row : data) {
	            MedidaCorriente unaMedida = new MedidaCorriente.Builder()
	                    .material(archivoCiclo[0])
	                    .tiempoStep(Double.parseDouble(row[0].trim()))
	                    .step(Integer.parseInt(row[1].trim()))
	                    .ciclo(Integer.parseInt(row[2].trim()))
	                    .corriente(Double.parseDouble(row[3].trim()))
	                    .voltaje(Double.parseDouble(row[4].trim()))
	                    .build();
	            unaMedida.setCapacidadDescarga((unaMedida.getTiempoStep() * unaMedida.getCorriente()) / 3600);

	            String claveBase = String.format("%s-%d-%d", archivoCiclo[0], unaMedida.getCiclo(), unaMedida.getStep());
	            int index = contadorIndices.getOrDefault(claveBase, 0);
	            contadorIndices.put(claveBase, index + 1);

	            String claveMedida = String.format("%s-%d", claveBase, index);
	            medidas.put(claveMedida, unaMedida.getCapacidadDescarga());
	        }
	        
	        guardarArchivoIntermedio(medidas, archivoCiclo);
	        
	        return medidas;
	    }
	    
	    public static void faseReduce(Map<String, Double> medidas, String[] archivoCiclo) {
	    	 Map<String, Double> maxMedidas = new HashMap<>();

	         List<String> mass = Arrays.asList(
	                 "Blanco;0.00092",
	                 "TrGOsulfurico;0.00113",
	                 "TrGOFosforico;0.00101",
	                 "GOSulfurico;0.00107",
	                 "GOFosforico;0.00088",
	                 "GOYPF;0.00096"
	         );

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
	                 double value = Math.abs(medida.getValue());

	                 if (!maxMedidas.containsKey(materialCicloKey) || maxMedidas.get(materialCicloKey) < value) {
	                     maxMedidas.put(materialCicloKey, value);
	                 }
	             }
	         }

	         String outputFolder = "concurrente";
	         File outputDir = new File(outputFolder);
	         if (!outputDir.exists()) {
	             outputDir.mkdirs();
	         }

	         String fileName = "output_" + String.format("%s-%s", archivoCiclo[0], archivoCiclo[1]) + "_benchmarking_material.txt";
	         String filePath = outputFolder + File.separator + fileName;

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
	    
	    public static void guardarArchivoIntermedio(Map<String, Double> valoresIntermedios, String[] archivoCiclo) {
	        String outputFolder = "concurrente";
	        File outputDir = new File(outputFolder);
	        if (!outputDir.exists()) {
	            outputDir.mkdirs();
	        }

	        String fileName = "intermedio_" + String.format("%s-%s", archivoCiclo[0], archivoCiclo[1]) + "_benchmarking_material.txt";
	        String filePath = outputFolder + File.separator + fileName;

	        try (FileWriter writer = new FileWriter(filePath)) {
	            for (Map.Entry<String, Double> entry : valoresIntermedios.entrySet()) {
	                String key = entry.getKey();
	                double value = entry.getValue();

	                String line = key + " : " + value + "\n";
	                writer.write(line);
	            }
	        } catch (IOException e) {
	            System.err.println("Error al escribir en el archivo intermedio: " + filePath);
	            e.printStackTrace();
	        }
	    }
}
