package es.um.poa.agents.seller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.AchieveREInitiator;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SellerAgent extends POAAgent  {
		
	private static final long serialVersionUID = 1L;
	
	private Stack<Lote> pilaLotes;
	private AID lonja;

	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			SellerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
				System.out.println("Vendedor " + this.getName() + " inicializado.");
				
				pilaLotes = new Stack<>();
				for (Lote l : config.getLotes()) {
					pilaLotes.push(l);
				}
				
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
				
				// Añadimos el protocolo de registro del vendedor
				ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
				aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				aux.addReceiver(lonja);
				aux.setConversationId(ConversationID.REGISTRO_VENDEDOR);
				sb.addSubBehaviour(new ProtocoloRegistroVendedorInitiator(this, aux));
				
				// Añadimos el protoco de deposito de las capturas
				sb.addSubBehaviour(new ProtocoloDepositoInitiator());
						
				addBehaviour(sb);
				// Añadimos el protocolo de registro del vendedor
//				ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
//				aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
//				aux.addReceiver(lonja);
//				aux.setConversationId(ConversationID.REGISTRO_VENDEDOR);
//				addBehaviour(new ProtocoloRegistroVendedorInitiator(this, aux));
//				
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
	
	@SuppressWarnings("serial")
	private class ProtocoloRegistroVendedorInitiator extends AchieveREInitiator {

		public ProtocoloRegistroVendedorInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
		}
		
		/**
		 * Este metodo es ejecutado justo antes del comienzo de la ejecucion 
		 * del comportamiento. Lo uso para imprimir mensajes de log.
		 */
		@Override
		public void onStart() {
			super.onStart();
			getLogger().info("ProtocoloRegistroVendedor - Initiator", "ProtocoloRegistroVendedor iniciado");
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
	
	@SuppressWarnings("serial")
	private class ProtocoloDepositoInitiator extends CyclicBehaviour {

		private int step = 0;
		private MessageTemplate template;
		private Lote lote = null;
		
		@Override
		public void action() {
			if (pilaLotes != null && !pilaLotes.isEmpty()) {
				switch (step) {
				case 0:
					getLogger().info("ProtocoloDeposito - Initiator", "ProtocoloDeposito iniciado");
					lote = pilaLotes.peek();
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					request.addReceiver(lonja);
					request.setConversationId(ConversationID.DEPOSITO_CAPTURA);
					request.setReplyWith("request" + System.currentTimeMillis());
					request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					try {
						request.setContentObject(lote);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					myAgent.send(request);
					
					template = MessageTemplate.and(MessageTemplate.MatchConversationId(ConversationID.DEPOSITO_CAPTURA),
							MessageTemplate.MatchInReplyTo(request.getReplyWith()));
					
					step = 1;
					break;
				case 1:
					ACLMessage respuesta = myAgent.receive(template);
					if (respuesta != null) {
						if (respuesta.getPerformative() == ACLMessage.INFORM) {
							//Deposito correcto
							getLogger().info("ProtocoloDeposito - Inititator", "Deposito correcto: " + lote.getTipo() + " - " + lote.getKg() + "kg");
							pilaLotes.pop();
						} else {
							//Deposito incorrecto
							getLogger().info("ProtocoloDeposito - Inititator", "Deposito incorrecto");
						}

						step = 0;
					} else {
						block();
					}
					
					break;
				}
			}
		}
		
	}
}
