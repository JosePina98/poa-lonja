package es.um.poa.agents.seller;

import java.util.List;

public class SellerAgentConfig {
	List<Lot> lots;

	@Override
	public String toString() {
		return "SellerAgentConfig [lots=" + lots + "]";
	}

	public List<Lot> getLots() {
		return lots;
	}

	public void setLots(List<Lot> lots) {
		this.lots = lots;
	}
}
