package es.um.poa.agents.seller;

import java.util.List;

/**
 * Clase que sirve para configurar los Agentes vendedores
 * mediante un fichero de configuracion YAML
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
public class SellerAgentConfig {
	List<Lote> lotes;

	@Override
	public String toString() {
		return "SellerAgentConfig [lotes=" + lotes + "]";
	}

	public List<Lote> getLotes() {
		return lotes;
	}

	public void setLotes(List<Lote> lotes) {
		this.lotes = lotes;
	}
}
