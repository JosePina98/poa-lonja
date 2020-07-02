package es.um.poa.agents.buyer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.protocols.addbuyer.AddBuyerProtocolInitiator;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class BuyerAgent extends POAAgent {
		
	private static final long serialVersionUID = 1L;

	public void setup() {
		super.setup();
		
		Object[] args = getArguments();
		if (args != null && args.length == 1) {
			String configFile = (String) args[0];
			BuyerAgentConfig config = initAgentFromConfigFile(configFile);
			
			if(config != null) {
		        
		        ACLMessage mt = new ACLMessage(ACLMessage.REQUEST);
				mt.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				mt.addReceiver(new AID("Lonja", AID.ISLOCALNAME));
				mt.setConversationId("AddBuyerProtocol");
				
				addBehaviour(new AddBuyerProtocolInitiator(this,mt));
				
				this.getLogger().info("INFO", "AddBuyerProtocol inititated");
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
}
