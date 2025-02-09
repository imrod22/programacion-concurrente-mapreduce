package bateria.catodo_li_s_benchmarking;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class MedidaAjustada {
	private final Map<String, Double> valoresIntermedios;
    private final Semaphore semaphore;

    public MedidaAjustada() {
        this.valoresIntermedios = new ConcurrentHashMap<>();
        this.semaphore = new Semaphore(1);
    }

    public void agregarMedidaAjustada(String key, Double value) throws InterruptedException {
        semaphore.acquire();
        try {
            valoresIntermedios.put(key, value);
        } finally {
            semaphore.release();
        }
    }

    public Map<String, Double> getValoresIntermedios() {
        return new HashMap<>(valoresIntermedios);
    }
}
