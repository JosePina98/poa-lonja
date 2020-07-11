package es.um.poa.agents.seller;

import java.io.Serializable;

/**
 * Clase que representa los lotes que se subastaran
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
public class Lote implements Serializable {

	private static final long serialVersionUID = 6226280235403786174L;
	private float kg;
    private String tipo;
    private float precioReserva;
    private float precioSalida;
    private float precioActual;
    
	@Override
	public String toString() {
		return "Lote [kg=" + kg + ", tipo=" + tipo + ", precioReserva=" + precioReserva + ", precioSalida=" + precioSalida + ", precioActual=" + precioActual + "]";
	}

	public float getKg() {
		return kg;
	}
	public void setKg(float kg) {
		this.kg = kg;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public float getPrecioSalida() {
		return precioSalida;
	}
	public void setPrecioSalida(float precioSalida) {
		this.precioSalida = precioSalida;
	}
	public float getPrecioReserva() {
		return precioReserva;
	}
	public void setPrecioReserva(float precioReserva) {
		this.precioReserva = precioReserva;
	}
	public float getPrecioActual() {
		return precioActual;
	}
	public void setPrecioActual(float precioActual) {
		this.precioActual = precioActual;
	}
	
}
