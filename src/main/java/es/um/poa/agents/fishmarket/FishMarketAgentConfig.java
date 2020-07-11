package es.um.poa.agents.fishmarket;

/**
 * Clase que sirve para configurar los Agentes Lonja
 * mediante un fichero de configuracion YAML
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
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
