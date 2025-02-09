package benchmarking_mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class BenchmarkingMaterial {
	public static void main(String[] args) throws Exception {
		
		Configuration config = new Configuration();
		Job job = Job.getInstance(config, "Benchmarking Catodo Bateria");

		//CONFIGURACION DEL JOB: Manipular la creacion de procesos para las 2 fases y balanceo de recursos.
		//job.setNumReduceTasks(4);

		//job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.minsize", 128 * 1024 * 1024);
		//job.getConfiguration().setLong("mapreduce.input.fileinputformat.split.maxsize", 256 * 1024 * 1024);

		//job.getConfiguration().setInt("mapreduce.map.memory.mb", 4096);
		//job.getConfiguration().setInt("mapreduce.reduce.memory.mb", 4096);
		//job.getConfiguration().set("mapreduce.map.java.opts", "-Xmx4096m");
		//job.getConfiguration().set("mapreduce.reduce.java.opts", "-Xmx4096m"); 

		//job.getConfiguration().setInt("mapreduce.shuffle.parallelcopies", 10);
		//job.getConfiguration().setInt("mapreduce.shuffle.memory.limit", 4096); 
		//
		//job.getConfiguration().setBoolean("mapreduce.map.output.compress", true);
		//job.getConfiguration().setBoolean("mapreduce.reduce.output.compress", true);

		//job.getConfiguration().set("mapreduce.task.io.sort.mb", "512");
		//job.getConfiguration().set("mapreduce.map.sort.spill.percent", "0.9");
		
		//job.setJarByClass(BenchmarkingMaterial.class);
		
		job.setMapperClass(BenchmarkingMaterialMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(DoubleWritable.class);
		
		job.setReducerClass(BenchmarkingMaterialReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));

	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
