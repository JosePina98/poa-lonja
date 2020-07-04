package es.um.poa.agents.seller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.AchieveREInitiator;
import jade.lang.acl.ACLMessage;

public class SellerAgent extends POAAgent  {
		
	private static final long serialVersionUID = 1L;
	
	private List<Lote> lotes;
	private AID lonja;

	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			SellerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
				System.out.println("Vendedor " + this.getName() + " inicializado.");
				
				lotes = config.getLotes();
				
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
				
				// Añadimos el protocolo de registro del vendedor
				ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
				aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				aux.addReceiver(lonja);
				aux.setConversationId(ConversationID.REGISTRO_VENDEDOR);
				addBehaviour(new ProtocoloRegistroVendedorInitiator(this, aux));
				
				this.getLogger().info("ProtocoloRegistroVendedor - Initiator", "ProtocoloRegistroVendedor iniciado");
		
			} else {
				doDelete();
			}
		} else {
			getLogger().info("ERROR", "Requiere fichero de cofiguracion.");
			doDelete();
		}
	}
	
	private SellerAgentConfig initAgentFromConfigFile(String fileName) {
		SellerAgentConfig config = null;
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
	
	/*
	private class ProtocoloRegistroVendedorInitiator extends Behaviour {

		private static final long serialVersionUID = 7805300819965261469L;
		private int step = 0;
		private MessageTemplate template; //Plantilla
		
		@Override
		public void action() {
			switch (step) {
			case 0:
				ACLMessage peticion = new ACLMessage(ACLMessage.REQUEST);
				peticion.addReceiver(lonja);
				peticion.setConversationId(ConversationID.REGISTRO_VENDEDOR);
				peticion.setReplyWith("peticion" + System.currentTimeMillis());
				myAgent.send(peticion);
				
				template = MessageTemplate.and(MessageTemplate.MatchConversationId(ConversationID.REGISTRO_VENDEDOR), 
							MessageTemplate.MatchInReplyTo(peticion.getReplyWith()));
				
				step = 1;
				break;	
			case 1:
				ACLMessage respuesta = myAgent.receive(template);
				
				if (respuesta != null) {
					if (respuesta.getPerformative() == ACLMessage.INFORM) {
						//Nos han admitido
						//Demas protocolos?
					} else {
						System.out.println("Fallo en el Registro - ProtocoloRegistroVendedor");
					}
					step = 2;
				} else {
					block();
				}
				
				break;
			}
		}

		@Override
		public boolean done() {
			if (step == 2) {
				return true;
			} else {
				return false;
			}
		}
		
	}
	*/
	
	@SuppressWarnings("serial")
	private class ProtocoloRegistroVendedorInitiator extends AchieveREInitiator {

		public ProtocoloRegistroVendedorInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		/**
		 * Maneja los mensajes inform recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleInform(ACLMessage msg) {
			super.handleInform(msg);
			getLogger().info("ProtocoloRegistroVendedor - Initiator", "INFORM recibido de \"" + msg.getSender().getLocalName() + "\", registro confirmado");
		}
		/**
		 * Maneja los mensajes failure recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleFailure(ACLMessage msg) {
			super.handleFailure(msg);
			getLogger().info("ProtocoloRegistroVendedor - Initiator", "FAILURE recibido de \"" + msg.getSender().getLocalName() + "\", registro fallido");
		}
		/**
		 * Maneja los mensajes refuse recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleRefuse(ACLMessage msg) {
			super.handleRefuse(msg);
			getLogger().info("ProtocoloRegistroVendedor - Initiator", "REFUSE recibido de \"" + msg.getSender().getLocalName() + "\", registro fallido");
		}
		/**
		 * Maneja los mensajes notUnderstood recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			super.handleNotUnderstood(msg);
			getLogger().info("ProtocoloRegistroVendedor - Initiator", "NOTUNDERSTOOD recibido de \"" + msg.getSender().getLocalName() + "\"");
		}
		
	}
}
