package benchmarking_mapreduce;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class BenchmarkingMaterialReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable>{

	private static final Map<String, Double> MASAS_PESO = new HashMap<>();
	
	@Override
	protected void setup(Context contextoActual) {
		MASAS_PESO.put("Blanco", 0.00092);
		MASAS_PESO.put("TrGOSulfurico", 0.00113);
		MASAS_PESO.put("TrGOFosforico", 0.00101);
		MASAS_PESO.put("GOSulfurico", 0.00107);
		MASAS_PESO.put("GOFosforico", 0.00088);
		MASAS_PESO.put("GOYPF", 0.00096);
	}
	
	@Override
	public void reduce(Text claveActual, Iterable<DoubleWritable> medidasActuales, Context contextoActual) throws IOException, InterruptedException {
		
		String[] formatoClave = claveActual.toString().split("-");
		String material = formatoClave[0];
		int step = Integer.parseInt(formatoClave[2]);
		
		if(step == 4) {
			double maximoActual = 0.0;
			
			for (DoubleWritable medida : medidasActuales) {
				double medidaActual = Math.abs(medida.get());
				if(maximoActual < medidaActual) {
					maximoActual = medidaActual;
				}
			}
			
			double pesoMasa = MASAS_PESO.getOrDefault(material, 1.0);
			double totalPorMasa = (maximoActual / pesoMasa) * 1000;
			
			contextoActual.write(claveActual, new DoubleWritable(totalPorMasa));
		}
		
	}
	
}
