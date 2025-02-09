package benchmarking_mapreduce;

public class MedidaCorriente {
	private String material;
	private double tiempoStep;
	private int ciclo;
	private int step;    
	private double corriente;
	private double voltaje;
	private double capacidadDescarga;
	
	public String getMaterial() {
		return material;
	}
	
	public double getTiempoStep() {
		return tiempoStep;
	}

	public int getCiclo() {
		return ciclo;
	}

	public int getStep() {
		return step;
	}

	public double getCorriente() {
		return corriente;
	}

	public double getVoltaje() {
		return voltaje;
	}

	public double getCapacidadDescarga() {
		return capacidadDescarga;
	}
	
	public void setCapacidadDescarga(double unaCapacidadDescarga) {
		this.capacidadDescarga = unaCapacidadDescarga;
	}
    
    public static class Builder {
    	MedidaCorriente medidaCorriente = new MedidaCorriente();

        public Builder() {
        }
        
        public Builder material(String material) {
        	medidaCorriente.material = material;
            return this;
        }

        public Builder tiempoStep(double tiempoStep) {
        	medidaCorriente.tiempoStep = tiempoStep;
            return this;
        }

        public Builder ciclo(int ciclo) {
        	medidaCorriente.ciclo = ciclo;
            return this;
        }

        public Builder step(int step) {
        	medidaCorriente.step = step;
            return this;
        }
        
        public Builder corriente(Double corriente) {
        	medidaCorriente.corriente = corriente;
            return this;
        }

        public Builder voltaje(Double voltaje) {
        	medidaCorriente.voltaje = voltaje;
            return this;
        }

        public MedidaCorriente build() {
            return medidaCorriente;
        }
    }
}
