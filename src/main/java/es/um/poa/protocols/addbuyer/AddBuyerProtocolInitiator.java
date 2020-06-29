package es.um.poa.protocols.addbuyer;

import es.um.poa.agents.buyer.BuyerAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Clase que modela el protocolo de a�adir un comprador
 * en la parte del initiator (el que implementa comprador).
 * 
 * @author Javier Martinez Valverde
 * @author Albano Castillo Callejas
 *
 */
@SuppressWarnings("serial")
public class AddBuyerProtocolInitiator extends AchieveREInitiator{

	/*
	 * Referencia al agente Comprador.
	 */
	private BuyerAgent buyer;
	
	/**
	 * Constructor
	 * 
	 * @param a Agente que implementa el protocolo.
	 * @param msg Mensaje ACL que enviara el agente.
	 */
	public AddBuyerProtocolInitiator(Agent a, ACLMessage msg) {
		
		// LLamamos al constructor del padre.
		super(a, msg);
		
		// Guardamos el agente que implemnta el protocolo (el agente comprador).
		try {
			buyer = (BuyerAgent) a;
		} catch (ClassCastException e) {
			// Si ocurre algún error informamos de ello y lo lanzamos hacia arriba.
			buyer.getLogger().info("ERROR", "The agent is not an instance of class that we expect (" + BuyerAgent.class.getName() + ").");
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * Maneja los mensajes inform recibidos.
	 * Se llama al padre y se añaden ordenes para la depuración.
	 */
	@Override
	protected void handleInform(ACLMessage msg) {
		super.handleInform(msg);
		buyer.getLogger().info("INFO", buyer.getLocalName() + ": INFORM response recived from " + msg.getSender().getLocalName() + " [AddBuyerProtocol].");
	}
	/**
	 * Maneja los mensajes failure recibidos.
	 * Se llama al padre y se añaden ordenes para la depuración.
	 */
	@Override
	protected void handleFailure(ACLMessage msg) {
		super.handleFailure(msg);
		buyer.getLogger().info("INFO",buyer.getLocalName() + ": FAILURE response recived from " + msg.getSender().getLocalName() + " [AddBuyerProtocol].");
	}
	/**
	 * Maneja los mensajes refuse recibidos.
	 * Se llama al padre y se añaden ordenes para la depuracion.
	 */
	@Override
	protected void handleRefuse(ACLMessage msg) {
		super.handleRefuse(msg);
		buyer.getLogger().info("INFO",buyer.getLocalName() + ": REFUSE response recived from " + msg.getSender().getLocalName() + " [AddBuyerProtocol].");
	}
	/**
	 * Maneja los mensajes notUnderstood recibidos.
	 * Se llama al padre y se aÑaden ordenes para la depuración.
	 */
	@Override
	protected void handleNotUnderstood(ACLMessage msg) {
		super.handleNotUnderstood(msg);
		buyer.getLogger().info("INFO",buyer.getLocalName() + ": NOTUNDERSTOOD response recived from " + msg.getSender().getLocalName() + " [AddBuyerProtocol].");
	}
}
