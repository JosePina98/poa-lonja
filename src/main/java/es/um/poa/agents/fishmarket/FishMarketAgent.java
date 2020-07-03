package es.um.poa.agents.fishmarket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.protocols.addbuyer.AddBuyerProtocolResponder;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FishMarketAgent extends POAAgent{
	
	private static final long serialVersionUID = 1L;

	private HashMap<AID, Float> mapaCompradores;
		
	public void setup() {
		super.setup();
		Object[] args = getArguments();
		String configFile = (String) args[0];
		FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
		
		if (args != null && args.length == 1) {
			
			if(config != null) {
								
				mapaCompradores = new HashMap<>();
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
				addBehaviour(new ProtocoloAdmisionCompradorResponder());
				this.getLogger().info("INFO", "ProtocoloAdmisionComprador anadido con exito");
								
				
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
	
	private class ProtocoloAdmisionCompradorResponder extends CyclicBehaviour {

		private static final long serialVersionUID = 4027527889320569181L;

		@Override
		public void action() {
			
			MessageTemplate templateAdmisionComprador = MessageTemplate.and(MessageTemplate.MatchConversationId(ConversationID.ADMISION_COMPRADOR),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			
			ACLMessage mensaje = myAgent.receive(templateAdmisionComprador);
			
			if (mensaje != null) {
				getLogger().info("ProtocoloAdmisionComprador - Responder", "Peticion para admitir un comprador recibida de " + mensaje.getSender().getLocalName());
				ACLMessage respuesta = mensaje.createReply();
				
				if (mapaCompradores.containsKey(mensaje.getSender())) {
					// Comprador ya registrado
					respuesta.setPerformative(ACLMessage.REFUSE);
					respuesta.setContent("El agente ya esta registrado");
					
					myAgent.send(respuesta);
					// TODO Cambiar por un mensaje original
					getLogger().info("ProtocoloAdmisionComprador - Responder", "El comprador " + mensaje.getSender().getLocalName() + " ya esta registrado");
				} else {
					// Registrar comprador
					mapaCompradores.put(mensaje.getSender(), 0F);
					respuesta.setPerformative(ACLMessage.INFORM);
					getLogger().info("ProtocoloAdmisionComprador - Responder", "Registro del comprador " + mensaje.getSender().getLocalName() + " aceptado");
				}
				
				myAgent.send(respuesta);
			} else {
				block();
			}
		}
		
	}
}
