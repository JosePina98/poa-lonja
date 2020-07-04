package es.um.poa.agents.buyer;

public class BuyerAgentConfig {
	private float presupuesto;
	
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
}
