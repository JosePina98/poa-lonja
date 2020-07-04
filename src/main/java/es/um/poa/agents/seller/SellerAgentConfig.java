package es.um.poa.agents.seller;

import java.util.List;

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
