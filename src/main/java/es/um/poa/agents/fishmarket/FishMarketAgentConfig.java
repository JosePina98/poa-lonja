package es.um.poa.agents.fishmarket;

public class FishMarketAgentConfig {
	private int numeroDeLineas;
	
	@Override
	public String toString() {
		return "[numeroDeLineas=" + numeroDeLineas + "]";
	}
	
	public int getNumeroDeLineas() {
		return numeroDeLineas;
	}
	public void setNumeroDeLineas(int numeroDeLineas) {
		this.numeroDeLineas = numeroDeLineas;
	}
}
