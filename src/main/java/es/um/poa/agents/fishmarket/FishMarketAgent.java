package es.um.poa.agents.fishmarket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.protocols.addbuyer.AddBuyerProtocolResponder;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;

public class FishMarketAgent extends POAAgent{
	
	private static final long serialVersionUID = 1L;

	public boolean performActionAddBuyerProtocol(AID sender) {
		
		// Hacer las Acciones correspondientes.
		return true;
	}
	
	public boolean checkActionAddBuyerProtocol(AID sender) {
		
		// Hacer las Acciones correspondientes.
		return true;
	}
	
	
	
	public void setup() {
		super.setup();
		Object[] args = getArguments();
		String configFile = (String) args[0];
		FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
		
		if (args != null && args.length == 1) {
			
			if(config != null) {
				
				
			// Crear los comportamientos correspondientes
/*
	        MessageTemplate messageTemplate = null;
	        		// Completa con el protocolo FIPA correspondiente y el mensajes correspondiente 
	        		//MessageTemplate.and(
	     		  	//MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.<>),
	     		  	//MessageTemplate.MatchPerformative(ACLMessage.<>) 
	     		  	//);
	        		
			
			
			 MessageTemplate templateAddBuyerProtocol = MessageTemplate.and(
					 messageTemplate, MessageTemplate.MatchConversationId("AddBuyerProtocol"));
		      
			 // Añadimos el protocolo de adicion del comprador.
			 addBehaviour(new AddBuyerProtocolResponder(this,templateAddBuyerProtocol));
			 this.getLogger().info("INFO", "AddBuyerProtocol sucessfully added");
*/		
				
				
				
				
				
				
			} else {
				doDelete();
			}
		} else {
			this.getLogger().info("ERROR", "Requiere fichero de configuración.");
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
}
