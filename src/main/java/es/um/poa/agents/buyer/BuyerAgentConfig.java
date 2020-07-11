package es.um.poa.agents.buyer;

import java.util.List;

/**
 * Clase que sirve para configurar los Agentes compradores
 * mediante un fichero de configuracion YAML
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
public class BuyerAgentConfig {
	private float presupuesto;
	private List<String> listaCompra;
	
	@Override
	public String toString() {
		return "BuyerAgentConfig [presupuesto=" + presupuesto + "]";
	}

	public float getPresupuesto() {
		return presupuesto;
	}

	public void setPresupuesto(float presupuesto) {
		this.presupuesto = presupuesto;
	}

	public List<String> getListaCompra() {
		return listaCompra;
	}
	
	public void setListaCompra(List<String> listaCompra) {
		this.listaCompra = listaCompra;
	}
}
