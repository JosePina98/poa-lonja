package es.um.poa.agents.fishmarket;

public class FishMarketAgentConfig {
	private int numberOfLanes;
	
	@Override
	public String toString() {
		return "[numberOfLanes=" + numberOfLanes + "]";
	}
	
	public int getNumberOfLanes() {
		return numberOfLanes;
	}
	public void setNumberOfLanes(int numberOfLanes) {
		this.numberOfLanes = numberOfLanes;
	}
}
