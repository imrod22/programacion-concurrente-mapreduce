package fetch_information.messi_x_tweets;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TweetAnalysis {

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();

		//PRUEBAS 2 y 3

		//conf.set("mapreduce.map.memory.mb", "2048");
		//conf.set("mapreduce.reduce.memory.mb", "3072");

		//conf.set("mapreduce.map.java.opts", "-Xmx1800m");
		//conf.set("mapreduce.reduce.java.opts", "-Xmx2560m");

        //conf.setInt("mapreduce.shuffle.parallelcopies", 2);
        //conf.setInt("mapreduce.shuffle.memory.limit", 1024);

		//conf.set("mapreduce.input.fileinputformat.split.maxsize", "16777216");
		//conf.set("mapreduce.input.fileinputformat.split.minsize", "8388608");

		//conf.set("mapreduce.input.fileinputformat.split.maxsize", "33554432");
		//conf.set("mapreduce.input.fileinputformat.split.minsize", "16777216");

		//conf.set("mapreduce.task.io.sort.mb", "128");
		//conf.set("mapreduce.map.sort.spill.percent", "0.8");
		//conf.set("mapreduce.job.reduces", "4");

		//conf.set("mapreduce.task.progress-report.interval", "1000");
		//conf.set("mapreduce.job.reduce.slowstart.completedmaps", "0.95");
		//conf.set("mapreduce.task.timeout", "600000");

		//conf.set("mapreduce.reduce.shuffle.input.buffer.percent", "0.5");
		//conf.set("mapreduce.reduce.shuffle.merge.percent", "0.4");
		//conf.set("mapreduce.reduce.shuffle.parallelcopies", "10");

		//PRUEBA 4

		//conf.set("mapreduce.map.memory.mb", "2048");
		//conf.set("mapreduce.reduce.memory.mb", "3072");
//
		//conf.set("mapreduce.map.java.opts", "-Xmx1800m");
		//conf.set("mapreduce.reduce.java.opts", "-Xmx2560m");
//
        //conf.setInt("mapreduce.shuffle.parallelcopies", 2);
        //conf.setInt("mapreduce.shuffle.memory.limit", 1024);
//
		//conf.set("mapreduce.input.fileinputformat.split.maxsize", "33554432");		
//
		//conf.set("mapreduce.reduce.shuffle.input.buffer.percent", "0.7");
		//conf.set("mapreduce.reduce.shuffle.merge.percent", "0.5");
//
		//conf.setInt("mapreduce.shuffle.parallelcopies", 20);
		//conf.setInt("mapreduce.reduce.shuffle.parallelcopies", 20);
//
		//conf.set("mapreduce.job.reduces", "2");
	     
	    Job job = Job.getInstance(conf, "Messi en Red Social X");
		//
		job.addFileToClassPath(new Path("/libs/javavader/vader-sentiment-analyzer-1.0.jar"));
		job.addFileToClassPath(new Path("/libs/javavader/commons-lang3-3.12.0.jar"));
		job.addFileToClassPath(new Path("/libs/javavader/opennlp-tools-2.0.0.jar"));
		job.addFileToClassPath(new Path("/libs/javavader/lucene-core-8.11.2.jar"));
		job.addFileToClassPath(new Path("/libs/javavader/lucene-analyzers-common-8.11.2.jar"));
	     
	    job.setJarByClass(TweetAnalysis.class);
	    job.setMapperClass(TweetAnalyzerMapper.class);
	    job.setCombinerClass(TweetAnalysisReducer.class);
	    job.setReducerClass(TweetAnalysisReducer.class);

	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);		

	     FileInputFormat.addInputPath(job, new Path(args[0]));
	     FileOutputFormat.setOutputPath(job, new Path(args[1]));
		 
	     System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
