package es.um.poa.agents.buyer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.agents.seller.Lote;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionInitiator;

public class BuyerAgent extends POAAgent {
		
	private static final long serialVersionUID = 1L;
	
	private float presupuesto;
	//private HashMap<String, Float> lotesComprados;
	private static final double PROBABILIDAD_PUJAR = 0.9;
	
	private AID lonja;
	
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
		        
				System.out.println("Comprador " + this.getName() + " inicializado.");
				
				//lotesComprados = new HashMap<>();
				presupuesto = config.getPresupuesto();
				
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

			if (recibido != null) {	
				
				//Si recibimos un PROPOSE
				if (recibido.getPerformative() == ACLMessage.PROPOSE) {
					try {
						lote = (Lote) recibido.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					if (lote != null) {
						getLogger().info("Nueva Subasta", "Nueva subasta recibida de \"" + recibido.getSender().getLocalName() + "\" de: " + lote.getTipo() + " - " + lote.getKg());

						if (Math.random() <= PROBABILIDAD_PUJAR) {
							ACLMessage accept = recibido.createReply();
							accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							accept.setConversationId(ConversationID.PUJAR);
							myAgent.send(accept);
							getLogger().info("Nueva Subasta", "SI PUJAMOS por: " + lote.getPrecioActual());
						} else {
							ACLMessage reject = recibido.createReply();
							reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
							reject.setConversationId(ConversationID.PUJAR);
							myAgent.send(reject);
							getLogger().info("Nueva Subasta", "NO pujamos por: " + lote.getPrecioActual());
						}
					}
					
				} else if (recibido.getPerformative() == ACLMessage.INFORM) { // Hemos ganado la puja
					try {
						lote = (Lote) recibido.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					if (lote != null) {
						getLogger().info("GANADOR", "Hemos ganado la subasta de \"" + recibido.getSender().getLocalName() + "\" de: " + lote.getTipo() + " - " + lote.getKg() + " por: " + lote.getPrecioActual() + "e");

						presupuesto -= lote.getPrecioActual();
					}
				} else if (recibido.getPerformative() == ACLMessage.REFUSE) { // Hemos pujado por mas dinero del que tenemos
					try {
						lote = (Lote) recibido.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					if (lote != null) {
						getLogger().info("TRAMPOSO", "No podemos pujar " + lote.getPrecioActual() + " si solo tenemos + " + presupuesto);
					}
				} else {
					block();
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
	/*
	@SuppressWarnings("serial")

		public ProtocoloSubscripcionInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		@Override
		protected void handleInform(ACLMessage inform) {
			// Por aqui nos avisa de que hay una nueva subasta
			Lote lote = null;
			try {
				lote = (Lote) inform.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			if (lote != null) {
				getLogger().info("Nueva Subasta", "Nueva subasta recibida de \"" + inform.getSender().getLocalName() + "\"");
				
				
				if (Math.random() >= PROBABILIDAD_PUJAR) {
					
					addBehaviour(new OneShotBehaviour() {
						
						@Override
						public void action() {
							ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
							request.addReceiver(lonja);
							request.setConversationId(ConversationID.PUJAR);
							request.setProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET);
							request.setContent(lote.getTipo());
						}
					});
				}
				
			}
		}
		
		@Override
		protected void handleAgree(ACLMessage agree) {
			super.handleAgree(agree);
			getLogger().info("ProtocoloSubscripcion - Initiator", "AGREE recibido de \"" + agree.getSender().getLocalName() + "\", subscripcion aceptada");
		}
		
		@Override
		protected void handleRefuse(ACLMessage refuse) {
			super.handleRefuse(refuse);
			getLogger().info("ProtocoloSubscripcion - Initiator", "REFUSE recibido de \"" + refuse.getSender().getLocalName() + "\", subscripcion denegada");
		}
		
		@Override
		protected void handleFailure(ACLMessage failure) {
			super.handleFailure(failure);
			getLogger().info("ProtocoloSubscripcion - Initiator", "FAILURE recibido de \"" + failure.getSender().getLocalName() + "\", subscripcion denegada");
		}
	}
	*/
}
