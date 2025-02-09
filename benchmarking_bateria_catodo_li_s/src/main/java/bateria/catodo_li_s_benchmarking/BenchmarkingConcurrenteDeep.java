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
import java.util.concurrent.locks.ReentrantLock;

public class BenchmarkingConcurrenteDeep {
	
	private static final int HILOS = Runtime.getRuntime().availableProcessors();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ReentrantLock LOCKER_LOG = new ReentrantLock();
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        String medidasPath = "data";

        File carpetaProcesar = new File(medidasPath);
        File[] archivosMedida = carpetaProcesar.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (archivosMedida == null || archivosMedida.length == 0) {
            System.out.println("No se encontraron archivos .txt en la carpeta.");
            return;
        }
        
        eventoLog("------ Iniciando procesamiento de " + archivosMedida.length + " archivos ------");

        ExecutorService executorArchivo = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> instanciasArchivo = new ArrayList<>();

        for (File archivo : archivosMedida) {
            Callable<Void> fileTask = () -> procesarArchivoMedida(archivo);
            instanciasArchivo.add(executorArchivo.submit(fileTask));
        }

        for (Future<Void> instancia : instanciasArchivo) {
        	instancia.get();
        }

        executorArchivo.shutdown();
        executorArchivo.awaitTermination(1, TimeUnit.MINUTES);
        
        eventoLog("------ Procesamiento completado. Todos los archivos han sido procesados ------");
    }
	
	private static void eventoLog(String message) {
		LOCKER_LOG.lock();
        try {
        	
            String tiempoFormateado = LocalDateTime.now().format(FORMATO_FECHA);
            String mensajeLog = String.format("[%s] %s%n", tiempoFormateado, message);
            
            try (FileWriter fw = new FileWriter("concurrente/log_benchmarking_material.txt", true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(mensajeLog);
            } catch (IOException e) {
                System.err.println("Error escribiendo en el log: " + e.getMessage());
            }
        } finally {
        	LOCKER_LOG.unlock();
        }
    }
	
    private static Void procesarArchivoMedida(File archivoActual) throws Exception {
        String rutaArchivo = archivoActual.getAbsolutePath();
        eventoLog("Iniciando procesamiento del archivo: " + rutaArchivo);

        String[] archivoInformacion = extraerNombreMaterial(rutaArchivo);
        MonitorMedidaAjuste controlajustes = new MonitorMedidaAjuste();
        MedidaAjustada resultadosAjuste = new MedidaAjustada();
        
        ExecutorService procesadorHilosEjecucion = Executors.newFixedThreadPool(HILOS);
        List<Future<Void>> instanciasEjecutadas = new ArrayList<>();

        for (int i = 0; i < HILOS; i++) {
        	instanciasEjecutadas.add(procesadorHilosEjecucion.submit(() -> procesarBloqueMedida(controlajustes, resultadosAjuste, archivoInformacion)));
        }

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\;");
                if (fields.length >= 5 && fields[0].trim().replace(".", "").matches("-?\\d+(\\.\\d+)?")) {
                	controlajustes.put(fields);
                }
            }
            controlajustes.setComplete();
        }

        for (Future<Void> unaEjecucion : instanciasEjecutadas) {
        	unaEjecucion.get();
        }

        procesadorHilosEjecucion.shutdown();
        procesadorHilosEjecucion.awaitTermination(1, TimeUnit.MINUTES);

        faseReduce(resultadosAjuste.getValoresIntermedios(), archivoInformacion);
        
        eventoLog("Finalizado procesamiento del archivo: " + rutaArchivo);
        return null;
    }
    
    private static Void procesarBloqueMedida(MonitorMedidaAjuste monitorAjuste, MedidaAjustada ajustes, String[] informacionArchivo) {
        try {
            while (true) {
                String[] fields = monitorAjuste.take();
                if (fields == null) break;

                MedidaCorriente unaMedida = new MedidaCorriente.Builder()
                        .material(informacionArchivo[0])
                        .tiempoStep(Double.parseDouble(fields[0].trim()))
                        .step(Integer.parseInt(fields[1].trim()))
                        .ciclo(Integer.parseInt(fields[2].trim()))
                        .corriente(Double.parseDouble(fields[3].trim()))
                        .voltaje(Double.parseDouble(fields[4].trim()))
                        .build();
                unaMedida.setCapacidadDescarga((unaMedida.getTiempoStep() * unaMedida.getCorriente()) / 3600);

                String claveBase = String.format("%s-%d-%d", informacionArchivo[0], unaMedida.getCiclo(), unaMedida.getStep());
                String claveMedida = String.format("%s-%d", claveBase, 0);
                ajustes.agregarMedidaAjustada(claveMedida, unaMedida.getCapacidadDescarga());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    
    public static String[] extraerNombreMaterial(String rutaArchivo) {
        Path pathArchivo = Paths.get(rutaArchivo);
        String nombreArchivo = pathArchivo.getFileName().toString();
        int extensionIndex = nombreArchivo.lastIndexOf(".");
        if (extensionIndex != -1) {
        	nombreArchivo = nombreArchivo.substring(0, extensionIndex);
        }
        return nombreArchivo.split("-");
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
    	 Map<String, Double> maximasMedidas = new HashMap<>();

         List<String> pesoMasas = Arrays.asList(
                 "Blanco;0.00092",
                 "TrGOSulfurico;0.00113",
                 "TrGOFosforico;0.00101",
                 "GOSulfurico;0.00107",
                 "GOFosforico;0.00088",
                 "GOYPF;0.00096"
         );

         Map<String, Double> pesoMasaMap = new HashMap<>();
         for (String masa : pesoMasas) {
             String[] parteMasa = masa.split(";");
             String material = parteMasa[0];
             double peso = Double.parseDouble(parteMasa[1]);
             pesoMasaMap.put(material, peso);
         }

         for (Map.Entry<String, Double> medida : medidas.entrySet()) {
             String claveActual = medida.getKey();
             String[] partes = claveActual.split("-");

             String material = partes[0];
             int ciclo = Integer.parseInt(partes[1]);
             int step = Integer.parseInt(partes[2]);

             if (step == 4) {
                 String materialCicloIdentificador = material + "-" + ciclo;
                 double ajuste = Math.abs(medida.getValue());

                 if (!maximasMedidas.containsKey(materialCicloIdentificador) || maximasMedidas.get(materialCicloIdentificador) < ajuste) {
                	 maximasMedidas.put(materialCicloIdentificador, ajuste);
                 }
             }
         }

         File carpetaOutput = new File("Concurrente");
         if (!carpetaOutput.exists()) {
        	 carpetaOutput.mkdirs();
         }

         String archivoOutput = "output_" + String.format("%s-%s", archivoCiclo[0], archivoCiclo[1]) + "_benchmarking_material.txt";
         String rutaArchivo = carpetaOutput + File.separator + archivoOutput;

         try (FileWriter writer = new FileWriter(rutaArchivo)) {
             for (Map.Entry<String, Double> maxima : maximasMedidas.entrySet()) {
                 String claveMaxima = maxima.getKey();
                 String material = claveMaxima.split("-")[0];
                 double valorCarga = maxima.getValue();
                 double pesoMasa = pesoMasaMap.get(material);
                 double totalPorMasa = (valorCarga / pesoMasa) * 1000;

                 String line = claveMaxima + " : " + totalPorMasa + "\n";
                 writer.write(line);
             }
         } catch (IOException e) {
             System.err.println("Error al escribir en el archivo: " + rutaArchivo);
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
