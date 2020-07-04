package es.um.poa.agents.fishmarket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.agents.seller.Lote;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class FishMarketAgent extends POAAgent{
	
	private static final long serialVersionUID = 1L;

	private HashMap<AID, Float> mapaCompradores;
	private HashMap<AID, List<Lote>> mapaVendedores;
		
	public void setup() {
		super.setup();
		Object[] args = getArguments();
		String configFile = (String) args[0];
		FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
		
		if (args != null && args.length == 1) {
			
			if(config != null) {
								
				mapaCompradores = new HashMap<>();
				mapaVendedores = new HashMap<>();

				// Crear los comportamientos correspondientes
				
				// Completa con el protocolo FIPA correspondiente y el mensajes correspondiente 
				DFAgentDescription dfd = new DFAgentDescription();
				dfd.setName(getAID());
				
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Lonja");
				sd.setName("Agente-Lonja");
				dfd.addServices(sd);
				try {
					DFService.register(this, dfd);
				} catch(FIPAException fe) {
					fe.printStackTrace();
				}
				// Añadimos el protocolo de adicion del comprador.
				MessageTemplate protocolo_admision_comprador = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.ADMISION_COMPRADOR));
				addBehaviour(new ProtocoloAdmisionCompradorResponder(this, protocolo_admision_comprador));
				this.getLogger().info("INFO", "ProtocoloAdmisionComprador anadido con exito");
				
				MessageTemplate protocolo_registro_vendedor = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.REGISTRO_VENDEDOR));
				addBehaviour(new ProtocoloRegistroVendedorResponder(this, protocolo_registro_vendedor));
				this.getLogger().info("INFO", "ProtocoloRegistroVendedor anadido con exito");
				
				MessageTemplate protocolo_apertura_credito = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.APERTURA_CREDITO));
				addBehaviour(new ProtocoloAperturaCreditoResponder(this, protocolo_apertura_credito));
				this.getLogger().info("INFO", "ProtocoloAperturaCredito anadido con exito");
								
				
			} else {
				doDelete();
			}
		} else {
			this.getLogger().info("ERROR", "Requiere fichero de configuracion.");
			doDelete();
		}
	}
	
	private FishMarketAgentConfig initAgentFromConfigFile(String fileName) {
		FishMarketAgentConfig config = null;
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
	private class ProtocoloAdmisionCompradorResponder extends AchieveREResponder {

		public ProtocoloAdmisionCompradorResponder(Agent a, MessageTemplate mt) {
			super(a, mt);
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
	
			getLogger().info("ProtocoloAdmisionComprador - Responder", "Mensaje REQUEST para admitir un Comprador recibido desde \"" + request.getSender().getLocalName() + "\"");
			// Comprobamos si podemos llevar a cabo la peticion.
			if (!mapaCompradores.containsKey(request.getSender())) {
	
				return null;
			}
			else {
				// Rechazamos a llevar a cabo la peticion.
				getLogger().info("ProtocoloAdmisionComprador - Responder", "El Comprador \"" + request.getSender().getLocalName() + "\" ya esta registrado");
				throw new RefuseException("check-failed");
			}
		}
		
		/**
		 * Metodo que lleva a cabo la peticion propuesta
		 * y se envia un INFORM.
		 * 
		 *  @param request  El mensaje recibido.
		 *  @param response La respuesta para el agente que ha realizado la peticion.
		 */
		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			
			// Llevamos a cabo la peticion.
			if (mapaCompradores.put(request.getSender(), 0F) == null) 
			{			
				// La peticion se ha realizado con exito.
				getLogger().info("ProtocoloAdmisionComprador - Responder", "Registro del Comprador \"" + request.getSender().getLocalName() + "\" aceptado");

				// Creamos y devolvemos el INFORM.
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				return inform;
			}
			else {
				// La peticion ha fallado, lanzamos un FAILURE
				throw new FailureException("unexpected-error");
			}	
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ProtocoloRegistroVendedorResponder extends AchieveREResponder {

		public ProtocoloRegistroVendedorResponder(Agent a, MessageTemplate mt) {
			super(a, mt);
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
	
			getLogger().info("ProtocoloRegistroVendedor - Responder", "Mensaje REQUEST para registrar un Vendedor recibido desde \"" + request.getSender().getLocalName() + "\"");
			// Comprobamos si podemos llevar a cabo la peticion.
			if (!mapaVendedores.containsKey(request.getSender())) {
	
				return null;
			}
			else {
				// Rechazamos a llevar a cabo la peticion.
				getLogger().info("ProtocoloRegistroVendedor - Responder", "El Vendedor \"" + request.getSender().getLocalName() + "\" ya esta registrado");
				throw new RefuseException("check-failed");
			}
		}
		
		/**
		 * Metodo que lleva a cabo la peticion propuesta
		 * y se envia un INFORM.
		 * 
		 *  @param request  El mensaje recibido.
		 *  @param response La respuesta para el agente que ha realizado la peticion.
		 */
		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			
			// Llevamos a cabo la peticion.
			if (mapaVendedores.put(request.getSender(), new LinkedList<Lote>()) == null) 
			{			
				// La peticion se ha realizado con exito.
				getLogger().info("ProtocoloRegistroVendedor - Responder", "Registro del Vendedor \"" + request.getSender().getLocalName() + "\" aceptado");

				// Creamos y devolvemos el INFORM.
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				return inform;
			}
			else {
				// La peticion ha fallado, lanzamos un FAILURE
				throw new FailureException("unexpected-error");
			}	
		}
		
	}
	
	@SuppressWarnings("serial")
	private class ProtocoloAperturaCreditoResponder extends AchieveREResponder {

		public ProtocoloAperturaCreditoResponder(Agent a, MessageTemplate mt) {
			super(a, mt);
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
	
			getLogger().info("ProtocoloAperturaCredito - Responder", "Mensaje REQUEST para abrir una linea de credito recibido de \"" + request.getSender().getLocalName() + "\"");
			// Comprobamos si podemos llevar a cabo la peticion.
			if (mapaCompradores.containsKey(request.getSender())) {
	
				return null;
			}
			else {
				// Rechazamos a llevar a cabo la peticion.
				getLogger().info("ProtocoloAperturaCredito - Responder", "El Comprador \"" + request.getSender().getLocalName() + "\" no esta registrado");
				throw new RefuseException("check-failed");
			}
		}
		
		/**
		 * Metodo que lleva a cabo la peticion propuesta
		 * y se envia un INFORM.
		 * 
		 *  @param request  El mensaje recibido.
		 *  @param response La respuesta para el agente que ha realizado la peticion.
		 */
		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			float credito = Float.valueOf(request.getContent());
			
			// Llevamos a cabo la peticion.
			if (credito > 0 && mapaCompradores.put(request.getSender(), credito) != null) 
			{			
				// La peticion se ha realizado con exito.
				getLogger().info("ProtocoloAperturaCredito - Responder", "Linea de credito del Comprador \"" + request.getSender().getLocalName() + "\" abierta con exito");

				// Creamos y devolvemos el INFORM.
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				return inform;
			}
			else {
				// La peticion ha fallado, lanzamos un FAILURE
				throw new FailureException("unexpected-error");
			}	
		}
		
	}
}
