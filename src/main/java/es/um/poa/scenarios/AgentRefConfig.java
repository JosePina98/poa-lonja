package es.um.poa.scenarios;

public class AgentRefConfig {
	private String name;
	private String config;
	
	@Override
	public String toString() {
		return "[name=" + name + ", config=" + config + "]";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	
	
}
