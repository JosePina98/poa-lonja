package es.um.poa.agents.buyer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.agents.seller.Lote;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

public class BuyerAgent extends POAAgent {
		
	private static final long serialVersionUID = 1L;
	
	private float presupuesto;
	private List<String> listaCompra;
	private List<Lote> lotesComprados;
	private static final double PROBABILIDAD_PUJAR = 0.4;
	
	private AID lonja;
	
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
		        
				System.out.println("Comprador " + this.getName() + " inicializado.");
				
				lotesComprados = new LinkedList<>();
				presupuesto = config.getPresupuesto();
				listaCompra = config.getListaCompra();

				// Buscamos al Agente Lonja
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Lonja");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(this, template);
					while (true) {
						result = DFService.search(this, template);
						try {
							Thread.sleep(3000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (result.length > 0) break;
					}
					lonja = result[0].getName();
					this.getLogger().info("INFO", "Agente Lonja encontrado");
				} catch (FIPAException f) {
					f.printStackTrace();
				}

				// Creo un comportamiento secuencial para que se ejecute un protocolo despues del otro
				SequentialBehaviour sb = new SequentialBehaviour();
				
				// Añado el protocolo de admisión del comprador
				ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
				aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				aux.addReceiver(lonja);
				aux.setConversationId(ConversationID.ADMISION_COMPRADOR);
				sb.addSubBehaviour(new ProtocoloAdmisionCompradorInitiator(this, aux));
				
				// Añado el protocolo de apertura de la linea de credito del comprador
				ACLMessage credito = new ACLMessage(ACLMessage.REQUEST);
				credito.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				credito.addReceiver(lonja);
				credito.setConversationId(ConversationID.APERTURA_CREDITO);
				credito.setContent(Float.toString(presupuesto));
				sb.addSubBehaviour(new ProtocoloAperturaCreditoInitiator(this, credito));
								
				sb.addSubBehaviour(new ProtocoloSubastaResponder());
				
				addBehaviour(sb);

			} else {
				doDelete();
			}
		} else {
			getLogger().info("ERROR", "Requiere fichero de cofiguracion.");
			doDelete();
		}
	}
	
	@Override
	public void takeDown() {

		// Printout a dismissal message
		System.out.println("Comprador \"" + getAID().getLocalName() + "\" finalizando");
		getLogger().info("INFO", "Comprador \"" + getAID().getLocalName() + "\" finalizando");
		super.takeDown();
	}
	
	private BuyerAgentConfig initAgentFromConfigFile(String fileName) {
		BuyerAgentConfig config = null;
		try {
			Yaml yaml = new Yaml();
			InputStream inputStream;
			inputStream = new FileInputStream(fileName);
			config = yaml.load(inputStream);
			getLogger().info("initAgentFromConfigFile", config.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return config;
	}

	
	@SuppressWarnings("serial")
	private class ProtocoloAdmisionCompradorInitiator extends AchieveREInitiator {

		public ProtocoloAdmisionCompradorInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		/**
		 * Este metodo es ejecutado justo antes del comienzo de la ejecucion 
		 * del comportamiento. Lo uso para imprimir mensajes de log.
		 */
		@Override
		public void onStart() {
			super.onStart();
			getLogger().info("ProtocoloAdmisionComprador - Initiator", "ProtocoloAdmisionComprador iniciado");
		}
		
		/**
		 * Maneja los mensajes inform recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleInform(ACLMessage msg) {
			super.handleInform(msg);
			getLogger().info("ProtocoloAdmisionComprador - Initiator", "INFORM recibido de \"" + msg.getSender().getLocalName() + "\", admision aceptada");
		}
		/**
		 * Maneja los mensajes failure recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleFailure(ACLMessage msg) {
			super.handleFailure(msg);
			getLogger().info("ProtocoloAdmisionComprador - Initiator", "FAILURE recibido de \"" + msg.getSender().getLocalName() + "\", admision denegada");
		}
		/**
		 * Maneja los mensajes refuse recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleRefuse(ACLMessage msg) {
			super.handleRefuse(msg);
			getLogger().info("ProtocoloAdmisionComprador - Initiator", "REFUSE recibido de \"" + msg.getSender().getLocalName() + "\", admision denegada");
		}
		/**
		 * Maneja los mensajes notUnderstood recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			super.handleNotUnderstood(msg);
			getLogger().info("ProtocoloAdmisionComprador - Initiator", "NOTUNDERSTOOD recibido de \"" + msg.getSender().getLocalName() + "\"");
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ProtocoloAperturaCreditoInitiator extends AchieveREInitiator {

		public ProtocoloAperturaCreditoInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		/**
		 * Este metodo es ejecutado justo antes del comienzo de la ejecucion 
		 * del comportamiento. Lo uso para imprimir mensajes de log.
		 */
		@Override
		public void onStart() {
			super.onStart();
			getLogger().info("ProtocoloAperturaCredito - Initiator", "ProtocoloAperturaCredito iniciado");
		}
		
		/**
		 * Maneja los mensajes inform recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleInform(ACLMessage msg) {
			super.handleInform(msg);
			getLogger().info("ProtocoloAperturaCredito - Initiator", "INFORM recibido de \"" + msg.getSender().getLocalName() + "\", linea de credito abierta");
		}
		/**
		 * Maneja los mensajes failure recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleFailure(ACLMessage msg) {
			super.handleFailure(msg);
			getLogger().info("ProtocoloAperturaCredito - Initiator", "FAILURE recibido de \"" + msg.getSender().getLocalName() + "\", apertura denegada");
		}
		/**
		 * Maneja los mensajes refuse recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleRefuse(ACLMessage msg) {
			super.handleRefuse(msg);
			getLogger().info("ProtocoloAperturaCredito - Initiator", "REFUSE recibido de \"" + msg.getSender().getLocalName() + "\", apertura denegada");
		}
		/**
		 * Maneja los mensajes notUnderstood recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			super.handleNotUnderstood(msg);
			getLogger().info("ProtocoloAperturaCredito - Initiator", "NOTUNDERSTOOD recibido de \"" + msg.getSender().getLocalName() + "\"");
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ProtocoloSubastaResponder extends Behaviour {
		
		@Override
		public void action() {

			ACLMessage recibido = myAgent.receive();
			
			Lote lote = null;

			if (recibido != null && 
					(recibido.getConversationId().equals(ConversationID.SUBASTA) || recibido.getConversationId().equals(ConversationID.PUJAR))) {	
				
				//Si recibimos un PROPOSE
				if (recibido.getPerformative() == ACLMessage.PROPOSE) {
					try {
						lote = (Lote) recibido.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					if (lote != null) {
						String mensajeLog = "";

						if (listaCompra.contains(lote.getTipo()) && Math.random() < PROBABILIDAD_PUJAR) {
							ACLMessage accept = recibido.createReply();
							accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							accept.setConversationId(ConversationID.PUJAR);
							myAgent.send(accept);
							mensajeLog += "SI pujamos por: ";
						} else {
							ACLMessage reject = recibido.createReply();
							reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
							reject.setConversationId(ConversationID.PUJAR);
							myAgent.send(reject);
							mensajeLog += "NO pujamos por: ";
						}
						mensajeLog += "Oferta de subasta recibida: " + lote.getKg() + "kg de " + lote.getTipo() + " por " + lote.getPrecioActual() + "e";
						getLogger().info("ProtocoloSubasta - Responder", mensajeLog);
					}
					
				} else {
					if (recibido.getPerformative() == ACLMessage.INFORM) { // Hemos ganado la puja
						try {
							lote = (Lote) recibido.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						
						if (lote != null) {
							getLogger().info("ProtocoloSubasta - Responder", "Hemos ganado la subasta de: " 
									+ lote.getKg() + "kg de " + lote.getTipo() + " por " + lote.getPrecioActual() + "e");
							
							listaCompra.remove(lote.getTipo());
								
							// Actualizamos nuestro presupuesto y añadimos el lote a la lista de lotes comprados
							// Esto es el equivalente al protocolo_retirada_compras
							presupuesto -= lote.getPrecioActual();
							lotesComprados.add(lote);
							getLogger().info("ProtocoloRetirada", "Se ha retirado correctamente el lote de: " + 
									lote.getKg() + "kg de " + lote.getTipo() + ". El presupuesto restante es " + presupuesto);
							
							if (listaCompra.isEmpty()) {
								getLogger().info("INFO", "El Comprador \"" + getAID().getLocalName() + "\" ha completado su lista de la compra");

								ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
								aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
								aux.addReceiver(lonja);
								aux.setConversationId(ConversationID.TERMINACION_COMPRADOR);
								addBehaviour(new ProtocoloTerminacionInitiator(myAgent, aux));
							}
						}
						
						
					} else if (recibido.getPerformative() == ACLMessage.REFUSE) { // Hemos pujado por mas dinero del que tenemos
						try {
							lote = (Lote) recibido.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						
						if (lote != null) {
							getLogger().info("ProtocoloSubasta - Responder", "No podemos pujar " + lote.getPrecioActual() + " si solo tenemos + " + presupuesto);
						}
						
					} else {
						block();
					}
				}
			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			return presupuesto <= 0;
		}
		
	}
	
//	private class ProtocoloRetiradaComprasInitiator extends AchieveREInitiator {
//
//		private static final long serialVersionUID = 1L;
//		
//		public ProtocoloRetiradaComprasInitiator(Agent a, ACLMessage msg) {
//			super(a, msg);
//		}
//		
//		/**
//		 * Este metodo es ejecutado justo antes del comienzo de la ejecucion 
//		 * del comportamiento. Lo uso para imprimir mensajes de log.
//		 */
//		@Override
//		public void onStart() {
//			super.onStart();
//			getLogger().info("ProtocoloRetirada - Initiator", "ProtocoloRetirada iniciado");
//		}
//		
//		@SuppressWarnings("unchecked")
//		@Override
//		protected void handleInform(ACLMessage inform) {
//			
//			List<Lote> listaLotes = null;
//			try {
//				listaLotes = (List<Lote>) inform.getContentObject();
//			} catch (UnreadableException e) {
//				e.printStackTrace();
//			}
//			
//			if (listaLotes != null) {
//				for (Lote lote : listaLotes) {
//					presupuesto -= lote.getPrecioActual();
//					lotesComprados.add(lote);
//					
//					getLogger().info("ProtocoloRetirada - Initiator", "Se ha retirado correctamente el lote de: " + 
//							lote.getKg() + "kg de " + lote.getTipo() + ". El presupuesto restante es " + presupuesto);
//				}
//				
//				myAgent.doDelete();
//			} else {
//				getLogger().info("ProtocoloRetirada - Initiator", "No se ha podido retirar ningun lote");
//			}
//		}
//		
//		/**
//		 * Maneja los mensajes failure recibidos.
//		 * Se llama al padre y se añaden ordenes para la depuracion.
//		 */
//		@Override
//		protected void handleFailure(ACLMessage msg) {
//			getLogger().info("ProtocoloRetirada - Initiator", "FAILURE recibido de \"" + msg.getSender().getLocalName() + "\", no queda ningun lote por retirar");
//			super.handleFailure(msg);
//		}
//		/**
//		 * Maneja los mensajes refuse recibidos.
//		 * Se llama al padre y se añaden ordenes para la depuracion.
//		 */
//		@Override
//		protected void handleRefuse(ACLMessage msg) {
//			getLogger().info("ProtocoloRetirada - Initiator", "REFUSE recibido de \"" + msg.getSender().getLocalName() + "\", no se ha podido retirar ningun lote");
//			super.handleRefuse(msg);
//		}
//	}
	
	@SuppressWarnings("serial")
	private class ProtocoloTerminacionInitiator extends AchieveREInitiator {

		public ProtocoloTerminacionInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		/**
		 * Este metodo es ejecutado justo antes del comienzo de la ejecucion 
		 * del comportamiento. Lo uso para imprimir mensajes de log.
		 */
		@Override
		public void onStart() {
			super.onStart();
			getLogger().info("ProtocoloTerminacion - Initiator", "ProtocoloTerminacion iniciado");
		}
		
		/**
		 * Maneja los mensajes inform recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleInform(ACLMessage msg) {
			super.handleInform(msg);
			myAgent.doDelete();
		}
	}
}
