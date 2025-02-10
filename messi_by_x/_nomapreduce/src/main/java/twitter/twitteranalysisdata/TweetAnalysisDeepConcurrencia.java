package twitter.twitteranalysisdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class TweetAnalysisDeepConcurrencia {
	
	   private static final Set<String> palabrasPositivas = new HashSet<>(Arrays.asList(
	            "cabra", "inolvidable", "pulga", "leo", "lio", "festejo", "ídolo", "mejor", "ganador", "campeón del mundo",
	            "ejemplo", "ganado", "campeones del Mundo", "copa del mundo", "record", "idolo", "baile", "emotivo", "grandeza",
	            "emocionante", "campeon mundial", "campeon copa america", "qatar", "mejor equipo", "vuelta", "mejor que", "👑", "⚽️",
	            "ganador", "dibu", "feliz", "legendario", "leyenda", "balon de oro", "💪", "mejor jugador", "scaloneta", "newells",
	            "rosario", "scaloni", "de paul", "barsa", "🤩", "😄", "🔥", "⭐", "🐐", "🇦🇷", "di maria"
	    ));

	    private static final Set<String> palabrasNegativas = new HashSet<>(Arrays.asList(
	            "peor que", "retiro", "pele", "decepcion", "pelé", "vinicius", "lesión", "lesion", "cristiano", "ronaldo", "suplente",
	            "no juega", "camina", "comparan", "comparacion", "Yamal", "👎", "😡", "😞", "🇧🇷", "🇵🇹", "🇲🇽", "😱", "👀", "perder",
	            "eliminado", "jamas", "le ganaron", "peligro", "sueldo", "contrato", "mexicanos", "🤡", "madrid", "😭"
	    ));
	    
	    private static final AtomicInteger positivos = new AtomicInteger(0);
	    private static final AtomicInteger negativos = new AtomicInteger(0);
	    private static final AtomicInteger neutrales = new AtomicInteger(0);
	    
	    private static StanfordCoreNLP pipelineES;
	    private static StanfordCoreNLP pipelineEN;
	    
	    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
	    
	    private static final ConcurrentHashMap<String, Boolean> processedMessages = new ConcurrentHashMap<>();

	    static {
	        Properties propsES = new Properties();
	        propsES.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
	        propsES.setProperty("tokenize.language", "es");
	        pipelineES = new StanfordCoreNLP(propsES);

	        Properties propsEN = new Properties();
	        propsEN.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
	        pipelineEN = new StanfordCoreNLP(propsEN);
	    }
	    
	    public static void main(String[] args) throws Exception {
	        String[] archivosInput = {
		            "tweets_messi_format_2025_compact.txt"
		        };

	        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter("concurrentecorenlp" + File.separator + "procesamiento_tweets_messi.txt", true))) {
	        
	            int numCores = Runtime.getRuntime().availableProcessors();
	            ExecutorService executorMap = Executors.newFixedThreadPool(numCores);
	            ExecutorService executorReduce = Executors.newFixedThreadPool(numCores);
	        
	        for (String inputFilePath : archivosInput) {
	        	
	        	System.out.println("Comenzando procesamiento de archivo: " + inputFilePath);
	        	long startFile = System.currentTimeMillis();
	        	
	            logWriter.write(sdf.format(new Date()) + " - Comenzando procesamiento de archivo: " + inputFilePath);
	            logWriter.newLine();
	            logWriter.flush();
	        	
	            procesarFaseMap(inputFilePath, executorMap); 

	            logWriter.write(sdf.format(new Date()) + " - Fase Map completada para archivo: " + inputFilePath + " - Archivos intermedios generados");
	            logWriter.newLine();
	            logWriter.flush();
	            
	            shuffleAndSort(inputFilePath);
	            
	            procesarFaseReduce(inputFilePath, executorReduce);

	            generarArchivoFinal(inputFilePath);

	            long endFile = System.currentTimeMillis();
	            long horas = TimeUnit.MILLISECONDS.toHours(endFile - startFile);
	            long minutos = TimeUnit.MILLISECONDS.toMinutes(endFile - startFile) % 60;
	            long segundos = TimeUnit.MILLISECONDS.toSeconds(endFile - startFile) % 60;

	            System.out.println("Comenzando procesamiento de archivo: " + inputFilePath + " - tiempo: " + String.format("%02d:%02d:%02d", horas, minutos, segundos));
	            
	            logWriter.write(sdf.format(new Date()) + " - Finalizando procesamiento de archivo: " + inputFilePath + " - tiempo total: " + String.format("%02d:%02d:%02d", horas, minutos, segundos));
	            logWriter.newLine();
	            logWriter.flush();
	        }
	      }
	        
	    }
 
	    private static void procesarFaseMap(String inputFilePath, ExecutorService executorMap) throws InterruptedException, IOException, ExecutionException {
	    	
	    	 List<String> tweets = obtenerTweetPosteados(inputFilePath);
	         Queue<String> tweetQueue = new ConcurrentLinkedQueue<>(tweets);
	         List<Future<?>> futures = new ArrayList<>();
	         ConcurrentLinkedQueue<String> resultados = new ConcurrentLinkedQueue<>();	         
	         
	         for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
	             futures.add(executorMap.submit(() -> {
	            	 
	                 while (!tweetQueue.isEmpty()) {
	                     String tweet = tweetQueue.poll();
	                     if (tweet != null) {
	                         String[] parts = tweet.split("\\|", 4);
	                         if (parts.length != 4) continue;

	                         String tweetId = parts[0].trim();
	                         String message = parts[2].trim().toLowerCase();

	                         if (!tweetId.matches("\\d+")) continue;
	                        	 
	                         String sentiment = analizarSentimientoMessi(message);
	                         resultados.add(tweetId + " | " + sentiment + "\n");
	                         
	                     }
	                 }
	    	
	    	
	                 try (BufferedWriter writer = new BufferedWriter(new FileWriter("concurrentecorenlp" + File.separator + "intermedios_messi_x_" + inputFilePath.replace("tweets_messi_format_", "")))) {
	                     for (String resultado : resultados) {
	                         writer.write(resultado);
	                     }
	                 } catch (IOException e) {
	                     e.printStackTrace();
	                 }
	             }));
	         }

	         for (Future<?> future : futures) future.get();

	    }
	    
	    private static List<String> obtenerTweetPosteados(String filePath) throws IOException {
	        List<String> tweets = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                tweets.add(line.trim());
	            }
	        }
	        return tweets;
	    }

	    private static String analizarSentimientoMessi(String tweet) {
	        for (String palabra : palabrasPositivas) {
	            if (tweet.contains(palabra)) return "Positive";
	        }
	        for (String palabra : palabrasNegativas) {
	            if (tweet.contains(palabra)) return "Negative";
	        }
	        return analizarSentimientoCoreNLP(tweet);
	    }
	    
	    private static String analizarSentimientoCoreNLP(String tweet) {
	        String sentimiento = analizarConModelo(tweet, pipelineES);
	        if (sentimiento.equals("Neutral")) {
	            sentimiento = analizarConModelo(tweet, pipelineEN);
	        }
	        return sentimiento;
	    }
	    
	    private static String analizarConModelo(String texto, StanfordCoreNLP pipeline) {
	        Annotation annotation = new Annotation(texto);
	        pipeline.annotate(annotation);
	        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
	            if (!sentiment.equals("Neutral")) return sentiment;
	        }
	        return "Neutral";
	    }
	    
	    private static void shuffleAndSort(String inputFilePath) throws IOException {
	        String archivoIntermedio = "concurrentecorenlp" + File.separator + "intermedios_messi_x_" + inputFilePath.replace("tweets_messi_format_", "");
	        
	        File archivoShuffleSort =  new File("concurrentecorenlp" + File.separator + "shufflesort_messi_x_" + inputFilePath.replace("tweets_messi_format_", ""));

	        List<String> lineas = new ArrayList<>();
	        try (BufferedReader reader = new BufferedReader(new FileReader(archivoIntermedio))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                lineas.add(line);
	            }
	        }
	        lineas.sort((linea1, linea2) -> {
	            String tweetId1 = linea1.split("\\|")[0].trim();
	            String tweetId2 = linea2.split("\\|")[0].trim();
	            return tweetId1.compareTo(tweetId2);
	        });

	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoShuffleSort))) {
	            for (String linea : lineas) {
	                writer.write(linea + "\n");
	            }
	        }
	    }
	    
	    private static void procesarFaseReduce(String inputFilePath, ExecutorService executorReduce) throws InterruptedException, ExecutionException {
	    	
	    	File archivoReduce =  new File("concurrentecorenlp" + File.separator + "shufflesort_messi_x_" + inputFilePath.replace("tweets_messi_format_", ""));

	         List<String> lineas = new ArrayList<>();
	         
	         try (BufferedReader reader = new BufferedReader(new FileReader(archivoReduce))) {
	             String line;
	             while ((line = reader.readLine()) != null) {
	                 lineas.add(line);
	             }
	         } catch (IOException e) {
	             e.printStackTrace();
	         }
	         
	         
	         Queue<String> lineaQueue = new ConcurrentLinkedQueue<>(lineas);
	         List<Future<?>> futures = new ArrayList<>();

	         for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
	             futures.add(executorReduce.submit(() -> {
	                 while (!lineaQueue.isEmpty()) {
	                     String linea = lineaQueue.poll();
	                     if (linea != null) {
	                         String[] parts = linea.split("\\|");
	                         if (parts.length > 1) {
	                             String sentiment = parts[1].trim();
	                             switch (sentiment) {
	                                 case "Positive": positivos.incrementAndGet(); break;
	                                 case "Negative": negativos.incrementAndGet(); break;
	                                 default: neutrales.incrementAndGet(); break;
	                             }
	                         }
	                     }
	                 }
	             }));
	         }

	         for (Future<?> future : futures) future.get();
	    }
	    
	    private static void generarArchivoFinal(String inputFilePath) {
	    	
	    	File outputFile = new File("concurrentecorenlp" + File.separator + "output_messi_x_" + inputFilePath.replace("tweets_messi_format_", ""));

	    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
	    	    writer.write("Positivos: " + positivos + "\n");
	    	    writer.write("Negativos: " + negativos + "\n");
	    	    writer.write("Neutrales: " + neutrales + "\n");
	    	} catch (IOException e) {
	    	    System.err.println("Error al guardar resultados finales: " + e.getMessage());
	    	}
	    }
}