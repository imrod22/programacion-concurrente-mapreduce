package twitter.twitteranalysisdata;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.ling.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TweetAnalysis {
	   public static void main(String[] args) {
		   
	        Properties props = new Properties();
	        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
	        props.setProperty("tokenize.language", "es");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	        
	        String[] inputFiles = {"tweets_messi_format_2025_compact.txt"};
	        
	        Set<String> palabrasPositivas = new HashSet<>(Arrays.asList("cabra", "inolvidable", "pulga", "leo", "lio", "festejo", "ídolo", "mejor", "ganador", "campeón del mundo", "ejemplo", "ganado", "campeones del Mundo", "copa del mundo", "record", "idolo", "baile", "emotivo", "grandeza", "emocionante", "campeon mundial", "campeon copa america", "qatar", "mejor equipo", "vuelta", "mejor que", "👑", "⚽️", "ganador", "dibu", "feliz", "legendario", "leyenda", "balon de oro", "💪", "mejor jugador", "scaloneta", "newells", "rosario", "scaloni", "de paul", "barsa", "🤩", "😄", "🔥", "⭐", "🐐", "🇦🇷", "di maria"));
	        Set<String> palabrasNegativas = new HashSet<>(Arrays.asList("peor que", "retiro", "pele", "decepcion", "pelé", "vinicius", "lesión", "lesion","cristiano", "ronaldo", "suplente", "no juega", "camina", "comparan", "comparacion", "Yamal", "👎", "😡", "😞", "🇧🇷", "🇵🇹", "🇲🇽", "😱", "👀", "perder", "eliminado", "jamas", "le ganaron", "peligro", "sueldo", "contrato", "mexicanos", "🤡", "madrid", "😭"));

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

	        System.out.println("Comenzando Tarea:" + sdf.format(new Date()));
	        Set<String> processedMessages = new HashSet<>();
	        
	        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter("secuencial" + File.separator + "procesamiento_tweets_messi.txt", true))) {
	            for (String inputFilePath : inputFiles) {
	                long startFile = System.currentTimeMillis();
	                logWriter.write(sdf.format(new Date()) + " - Comenzando procesamiento de archivo: " + inputFilePath);
	                logWriter.newLine();	                
	                
	                List<String> rawData = new ArrayList<>();
	                try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
	                    String line;
	                    while ((line = br.readLine()) != null) {
	                        rawData.add(line.trim());
	                    }
	                }
	                
	                int positivos = 0, negativos = 0, neutrales = 0;
	                List<String> analyzedData = new ArrayList<>();
	                
	                for (String tweet : rawData) {
	                    if (tweet.isEmpty()) {
	                        continue;
	                    }
	                    
	                    String[] parts = tweet.split("\\|", 4);
	                    if (parts.length < 4) {
	                        continue;
	                    }
	                    String tweetId = parts[0].trim();
	                    String message = parts[2].trim().toLowerCase();
	                    
	                    if (processedMessages.contains(message)) {
	                        continue;
	                    }
	                    
	                    processedMessages.add(message);
	                    String sentiment = analizarPalabrasDestacas(message, palabrasPositivas, palabrasNegativas);
	                    
                        if ("Neutral".equals(sentiment)) {
                        	Annotation annotation = new Annotation(message);
	                    	pipeline.annotate(annotation);
	                    	sentiment = annotation.get(CoreAnnotations.SentencesAnnotation.class).stream()
	                            .map(sentence -> sentence.get(SentimentCoreAnnotations.SentimentClass.class))
	                            .findFirst()
	                            .orElse("Neutral");
                        }
	                    
                        if ("Neutral".equals(sentiment)) {
	                        Properties englishProps = new Properties();
	                        englishProps.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
	                        StanfordCoreNLP englishPipeline = new StanfordCoreNLP(englishProps);
	                        Annotation englishAnnotation = new Annotation(message);
	                        englishPipeline.annotate(englishAnnotation);
	                        sentiment = englishAnnotation.get(CoreAnnotations.SentencesAnnotation.class).stream()
	                                .map(sentence -> sentence.get(SentimentCoreAnnotations.SentimentClass.class))
	                                .findFirst()
	                                .orElse("Neutral");
	                    }
                        
	                    switch (sentiment) {
	                        case "Positive":
	                            positivos++;
	                            break;
	                        case "Negative":
	                            negativos++;
	                            break;
	                        default:
	                            neutrales++;
	                            break;
	                    }
	                    analyzedData.add(tweetId + " | " + sentiment);
	                }	  
	                
	                String intermediateFilePath = "secuencial" + File.separator + "intermedios_messi_x_secuencial_" + inputFilePath.replace("tweets_", "");
	                try (BufferedWriter bw = new BufferedWriter(new FileWriter(intermediateFilePath))) {
	                    for (String analyzedLine : analyzedData) {
	                        bw.write(analyzedLine);
	                        bw.newLine();
	                    }
	                }
	                long endFile = System.currentTimeMillis();
	                long horas = TimeUnit.MILLISECONDS.toHours(endFile - startFile);
	                long minutos = TimeUnit.MILLISECONDS.toMinutes(endFile - startFile) % 60;
	                long segundos = TimeUnit.MILLISECONDS.toSeconds(endFile - startFile) % 60;

	                System.out.println("Tiempo para procesar " + inputFilePath + ": " + 
	                                   String.format("%02d:%02d:%02d", horas, minutos, segundos));

	                logWriter.write(sdf.format(new Date()) + " - Finalizando procesamiento de archivo: " 
	                                + inputFilePath + " - tiempo total: " 
	                                + String.format("%02d:%02d:%02d", horas, minutos, segundos));
	                logWriter.newLine();
	 	        
	 	            String summaryFilePath = "secuencial" + File.separator + "output_messi_x_secuencial_" + inputFilePath.replace("tweets_", "");
	                 try (BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFilePath))) {
	                     bw.write("Positivos: " + positivos);
	                     bw.newLine();
	                     bw.write("Negativos: " + negativos);
	                     bw.newLine();
	                     bw.write("Neutrales: " + neutrales);
	                     bw.newLine();
	                  }
	            }	            
	        }        
	        
	        catch (IOException e) {
	        	 System.err.println("Error procesando los archivos: " + e.getMessage());
	        }
	    }
	   
	   private static String analizarPalabrasDestacas(String message, Set<String> palabrasPositivas, Set<String> palabrasNegativas) {
	        for (String palabrapositiva : palabrasPositivas) {
	            if (message.contains(palabrapositiva.toLowerCase())) {
	                return "Positive";
	            }
	        }
	        for (String palabranegativa : palabrasNegativas) {
	            if (message.contains(palabranegativa.toLowerCase())) {
	                return "Negative";
	            }
	        }
	        return "Neutral";
	    }
}
