package benchmarking_mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class BenchmarkingMaterialMapper extends Mapper<Object, Text, Text, DoubleWritable>{

	private final Text claveIntermedia = new Text();
	private final DoubleWritable valorIntermedio = new DoubleWritable();
	
	@Override
	public void map(Object materialActual, Text medicionActual, Context contextoActual) throws IOException, InterruptedException {
		String[] medidas = medicionActual.toString().split("\\;");
		
			try {
				String rutaArchivo = ((FileSplit) contextoActual.getInputSplit()).getPath().getName();
				String[] informacionArchivo = extraerInformacionArchivo(rutaArchivo);
				String material = informacionArchivo[0];
				
				MedidaCorriente medida = new MedidaCorriente.Builder()
                        .material(material)
                        .tiempoStep(Double.parseDouble(medidas[0].trim()))
                        .step(Integer.parseInt(medidas[1].trim()))
                        .ciclo(Integer.parseInt(medidas[2].trim()))
                        .corriente(Double.parseDouble(medidas[3].trim()))
                        .voltaje(Double.parseDouble(medidas[4].trim()))
                        .build();
				
				medida.setCapacidadDescarga((medida.getTiempoStep() * medida.getCorriente()) / 3600);
				String claveActual = String.format("%s-%d-%d", material, medida.getCiclo(), medida.getStep());
				claveIntermedia.set(claveActual);
				valorIntermedio.set(medida.getCapacidadDescarga());
				
				contextoActual.write(claveIntermedia, valorIntermedio);
			}
			catch(NumberFormatException e){
				contextoActual.getCounter("Map", "ParseErrors").increment(1);
			}
	}
	
	private static String[] extraerInformacionArchivo(String rutaArchivo) {
        String nombreArchivo = new Path(rutaArchivo).getName();
        int extensionIndex = nombreArchivo.lastIndexOf(".");
        
        if (extensionIndex != -1) {
        	nombreArchivo = nombreArchivo.substring(0, extensionIndex);
        }
        return nombreArchivo.split("-");
	}
	
	
}
