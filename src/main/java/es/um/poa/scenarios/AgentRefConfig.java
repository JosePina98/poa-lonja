package es.um.poa.scenarios;

/**
 * Clase que sirve para configurar establecer los ficheros
 * de configuracion de cada Agente
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
public class AgentRefConfig {
	private String nombre;
	private String configuracion;
	
	@Override
	public String toString() {
		return "[nombre=" + nombre + ", configuracion=" + configuracion + "]";
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getConfiguracion() {
		return configuracion;
	}
	public void setConfiguracion(String configuracion) {
		this.configuracion = configuracion;
	}
	
	
}
