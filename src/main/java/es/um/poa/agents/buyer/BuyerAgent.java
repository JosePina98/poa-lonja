package es.um.poa.agents.buyer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.protocols.addbuyer.AddBuyerProtocolInitiator;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BuyerAgent extends POAAgent {
		
	private static final long serialVersionUID = 1L;
	
	private float presupuesto;
	private HashMap<String, Float> lotesComprados;
	
	private AID lonja;
	
	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
		        
				System.out.println("Comprador " + this.getName() + " inicializado.");
				
				lotesComprados = new HashMap<>();
				presupuesto = config.getBudget();
				
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
					this.getLogger().info("INFO", "Agente Lonja encontrado.");
				} catch (FIPAException f) {
					f.printStackTrace();
				}
				
				addBehaviour(new ProtocoloAdmisionCompradorInitiator());
				
				this.getLogger().info("ProtocoloAdmisionComprador - Initiator", "ProtocoloAdmisionComprador iniciado");
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
	
	private class ProtocoloAdmisionCompradorInitiator extends Behaviour {

		private static final long serialVersionUID = 1362506647955406907L;
		private int step = 0;
		private MessageTemplate template; //Plantilla
		
		@Override
		public void action() {
			switch(step) {
			case 0:
				ACLMessage peticion = new ACLMessage(ACLMessage.REQUEST);
				peticion.addReceiver(lonja);
				peticion.setConversationId(ConversationID.ADMISION_COMPRADOR);
				peticion.setReplyWith("req" + System.currentTimeMillis());
				myAgent.send(peticion);
				
				template = MessageTemplate.and(MessageTemplate.MatchConversationId(ConversationID.ADMISION_COMPRADOR), 
							MessageTemplate.MatchInReplyTo(peticion.getReplyWith()));
				
				step = 1;
				break;	
			
			case 1:
				ACLMessage respuesta = myAgent.receive(template);
				
				if (respuesta != null) {
					if (respuesta.getPerformative() == ACLMessage.INFORM) {
						//Nos han admitido
						//getLogger().info("ProtocoloAdmisionCompradorInitiator", "Registro Aceptado");
						//ProtocoloAperturaCredito?
					} else {
						System.out.println("Fallo en el Registro - ProtocoloAdmisionComprador");
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
}
