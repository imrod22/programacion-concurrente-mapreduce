package fetch_information.messi_x_tweets;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import com.vader.sentiment.analyzer.SentimentAnalyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TweetAnalyzerMapper extends Mapper<Object, Text, Text, IntWritable> {

    private Set<String> palabrasPositivas;
    private Set<String> palabrasNegativas;
    private final static IntWritable uno = new IntWritable(1);
    private Text sentiment = new Text();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        palabrasPositivas = new HashSet<>(Arrays.asList(
                "goat", "lionel", "unforgettable", "leo", "lio", "goal", "celebration", "idol", "best",
                "winner", "world champion", "example", "won", "world cup", "record", "idol", "maradona",
                "dance", "emotional", "greatness", "exciting", "world champion", "copa america champion", "qatar",
                "best team", "comeback", "better than", "👑", "⚽️", "won", "messi's goal", "dibu",
                "what a player", "happy", "legendary", "ballon d'or", "💪", "best player", "scaloneta",
                "newell's", "rosario", "scaloni", "de paul", "barsa", "antonella", "🤩", "😄", "🔥",
                "⭐", "🐐", "🇦🇷", "di maria"
        ));

        palabrasNegativas = new HashSet<>(Arrays.asList(
                "worse than", "retirement", "pele", "disappointment", "vinicius", "injury", "injured", "cristiano",
                "ronaldo", "substitute", "doesn't play", "walks", "compare", "comparison", "yamal", "👎", "😡", "😞",
                "🇧🇷", "🇵🇹", "🇲🇽", "😱", "👀", "lose", "eliminated", "never", "they beat him",
                "salary", "contract", "mexicans", "🤡", "madrid", "😭"
        ));
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String tweet = value.toString();
        if (tweet.isEmpty()) return;
        
        String[] parts = tweet.split("\\|", 4);
        if (parts.length < 4) return;

        String message = parts[2].trim().toLowerCase();
        if (message.length() > 500) {
            message = message.substring(0, 500);
        }
        
        if (!message.contains("messi")) return;

        sentiment.set("Neutral");

        if (palabrasPositivas.stream().anyMatch(message::contains)) {
            sentiment.set("Positive");
        } else if (palabrasNegativas.stream().anyMatch(message::contains)) {
            sentiment.set("Negative");
        } else {

                SentimentAnalyzer analyzer = new SentimentAnalyzer(message);
                Map<String, Float> polarityScores = analyzer.getPolarity();
    
                if (polarityScores != null && polarityScores.containsKey("compound")){
                    
                double compoundScore = polarityScores.get("compound");
    
                if (compoundScore >= 0.05) {
                    sentiment.set("Positive");
                } else if (compoundScore <= -0.05) {
                    sentiment.set("Negative");
                }                

                }
        }
        context.write(sentiment, uno);
    }
}