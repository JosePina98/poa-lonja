package es.um.poa.scenarios;

import java.util.List;


/**
 * Define los parametros que conforman un escenario para su simulacion desde un fichero YAML.
 * Ref: https://www.baeldung.com/java-snake-yaml
 * 
 *
 */
public class ScenarioConfig {
	private String nombre;
	private String descripcion;
	private AgentRefConfig lonja;
	private List<AgentRefConfig> compradores;
	private List<AgentRefConfig> vendedores;
	
	@Override
	public String toString() {
		return "ScenarioConfig [nombre=" + nombre + ", descripcion=" + descripcion + ",\n"+
				"lonja=" + lonja + ",\n"+
				"compradores=" + compradores + ",\n"+
				"vendedores=" + vendedores + "]";
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public AgentRefConfig getLonja() {
		return lonja;
	}

	public void setLonja(AgentRefConfig lonja) {
		this.lonja = lonja;
	}

	public List<AgentRefConfig> getCompradores() {
		return compradores;
	}

	public void setCompradores(List<AgentRefConfig> compradores) {
		this.compradores = compradores;
	}

	public List<AgentRefConfig> getVendedores() {
		return vendedores;
	}

	public void setVendedores(List<AgentRefConfig> vendedores) {
		this.vendedores = vendedores;
	}

}
