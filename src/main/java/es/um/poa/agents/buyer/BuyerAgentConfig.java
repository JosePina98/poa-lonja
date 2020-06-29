package es.um.poa.agents.buyer;

public class BuyerAgentConfig {
	private float budget;
	
	@Override
	public String toString() {
		return "BuyerAgentConfig [budget=" + budget + "]";
	}

	public float getBudget() {
		return budget;
	}

	public void setBudget(float budget) {
		this.budget = budget;
	}
}
