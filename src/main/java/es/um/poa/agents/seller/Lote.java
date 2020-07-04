package es.um.poa.agents.seller;

public class Lote {
    private float kg;
    private String type;
        
	@Override
	public String toString() {
		return "Lot [kg=" + kg + ", type=" + type + "]";
	}

	public float getKg() {
		return kg;
	}
	public void setKg(float kg) {
		this.kg = kg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
