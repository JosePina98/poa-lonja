package es.um.poa.protocols.addbuyer;

import es.um.poa.agents.fishmarket.FishMarketAgent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * Clase que modela el protocolo de a�adir un comprador
 * en la parte del responder 
 *
 */
@SuppressWarnings("serial")
public class AddBuyerProtocolResponder extends AchieveREResponder 
{

	/*
	 * Referencia al agente Lonja.
	 */
	private FishMarketAgent fishMarket;
	
	/**
	 * Constructor
	 * 
	 * @param a Agente que implementa el protocolo.
	 * @param mt Plantilla del mensaje para distinguirlo del de otros protocolos.
	 */
	public AddBuyerProtocolResponder(Agent a, MessageTemplate mt) {
		
		// LLamamos al constructor del padre.
		super(a, mt);
		
		// Guardamos el agente que implemnta el protocolo (el agente lonja).
		try {
			fishMarket = (FishMarketAgent) a;
		} catch (ClassCastException e) {
			// Si hay error informamos de ello y lo lanzamos hacia arriba.
			fishMarket.getLogger().info("ERROR","The agent is not an instance of class that we expect (" + FishMarketAgent.class.getName() + ").");
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Metodo que prepara la respuesta a la peticion.
	 * En caso de acceder a la peticion se obvia el AGREE
	 * sino se manda un REFUSE.
	 * 
	 * @param request El mensaje recibido.
	 */
	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		
		System.out.println();
		fishMarket.getLogger().info("INFO",fishMarket.getLocalName() + ": REQUEST to admit a buyer received from " + request.getSender().getLocalName());
		// Comprobamos si podemos llevar a cabo la peticion.
		if (fishMarket.checkActionAddBuyerProtocol(request.getSender())) {

			return null;
		}
		else {
			// Rechazamos a llevar a cabo la peticion.
			throw new RefuseException("check-failed");
		}
	}
	
	/**
	 * Metodo que lleva a cabo la peticion propuesta
	 * y se envía un INFORM.
	 * 
	 *  @param request  El mensaje recibido.
	 *  @param response La respuesta para el agente que ha realizado la petición.
	 */
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		
		// Llevamos a cabo la peticion.
		if (fishMarket.performActionAddBuyerProtocol(request.getSender())) 
		{			
			// La peticion se ha realizado con exito.
			fishMarket.getLogger().info("INFO",fishMarket.getLocalName() + ": Action successfully performed for " + request.getSender().getLocalName() + " [AddBuyerProtocol]");

			// Creamos y devolvemos el INFORM.
			ACLMessage inform = request.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			return inform;
		}
		else {
			// La peticion ha fallado, lanzamos un FAILURE
			fishMarket.getLogger().info("INFO",fishMarket.getLocalName() + ": Action failed for " + request.getSender().getLocalName() + " [AddBuyerProtocol]");
			throw new FailureException("unexpected-error");
		}	
	}
}
