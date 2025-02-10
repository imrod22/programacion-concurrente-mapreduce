package fetch_information.messi_x_tweets;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class TweetAnalysisReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
    private IntWritable calificacionTweets = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int sumaOcurrencia = 0;
        
        for (IntWritable tweet : values) {
        	sumaOcurrencia += tweet.get();
        }
        calificacionTweets.set(sumaOcurrencia);
        context.write(key, calificacionTweets);
    }
}
