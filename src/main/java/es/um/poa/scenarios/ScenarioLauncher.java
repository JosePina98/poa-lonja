package es.um.poa.scenarios;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import jade.util.Logger;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.utils.AgentLoggingHTMLFormatter;
import jade.core.Runtime;
import jade.tools.sniffer.Sniffer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

/**
 * Clase Principal que se encarga de lanzar el escenario
 * 
 * @author Jose Antonio Pina Gomez
 * 
 */
public class ScenarioLauncher {
	
	//  LOS AGENTES EN EL SNIFFER
	static List<String> simulationAgents = new LinkedList<String>();
	
	public static void main(String[] args) throws SecurityException, IOException {
		if(args.length == 1) {
			String config_file = args[0];
			Yaml yaml = new Yaml();
			InputStream inputStream = new FileInputStream(config_file);
			ScenarioConfig scenario = yaml.load(inputStream);
			
			initLogging(scenario.getNombre());
			
			System.out.println(scenario);
			try {

				// Obtenemos una instancia del entorno runtime de Jade
				Runtime rt = Runtime.instance();
				
				// Terminamos la máquinq virtual si no hubiera ningún contenedor de agentes activo
				rt.setCloseVM(true);
				
				// Lanzamos una plataforma en el puerto 8888
				// Y creamos un profile de la plataforma a partir de la cual podemos
				// crear contenedores
				Profile pMain = new ProfileImpl(null, 8888, null);
				System.out.println("Lanzamos una plataforma desde clase principal... " + pMain);
				
				// Creamos el contenedor
				AgentContainer mc = rt.createMainContainer(pMain);
				
				// Creamos un RMA (la GUI de JADE)
				System.out.println("Lanzando el agente RMA en el contenedor main ...");
				AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
				rma.start();

				// FishMarket
				AgentRefConfig marketConfig = scenario.getLonja();
				Object[] marketConfigArg = {marketConfig.getConfiguracion()};
				simulationAgents.add(marketConfig.getNombre());
				AgentController market = mc.createNewAgent(
						marketConfig.getNombre(), 
						es.um.poa.agents.fishmarket.FishMarketAgent.class.getName(), 
						marketConfigArg);
				market.start();

				// Buyers
				List<AgentRefConfig> buyers = scenario.getCompradores();
				for(AgentRefConfig buyer: buyers) {

					Object[] buyerConfigArg = {buyer.getConfiguracion()};
					System.out.println(buyer);
					simulationAgents.add(buyer.getNombre());

					AgentController b = mc.createNewAgent(
							buyer.getNombre(), 
							es.um.poa.agents.buyer.BuyerAgent.class.getName(), 
							buyerConfigArg);
					b.start();
				}

				// Sellers
				List<AgentRefConfig> sellers = scenario.getVendedores();
				for(AgentRefConfig seller: sellers) {
					System.out.println(seller);
					Object[] buyerConfigArg = {seller.getConfiguracion()};
					
					simulationAgents.add(seller.getNombre());
					AgentController b = mc.createNewAgent(
							seller.getNombre(), 
							es.um.poa.agents.seller.SellerAgent.class.getName(), 
							buyerConfigArg);
					b.start();
				}
			
				addSniffer(mc,simulationAgents);
					
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initLogging(String scenarioName) throws SecurityException, IOException {
	      LogManager lm = LogManager.getLogManager();
	      
	      Logger logger = Logger.getMyLogger("es.um.poa");
	      logger.setLevel(Level.INFO);
	      
	      FileHandler html_handler = new FileHandler("logs/"+scenarioName+".html");
	      html_handler.setFormatter(new AgentLoggingHTMLFormatter());
	      logger.addHandler(html_handler);

	      lm.addLogger(logger);
	}
	
	/**
	 * Metodo para incluir el agente sniffer al contenedor principal de agentes. 
	 * @param mc Contenedor principal de agentes.
	 * @param agents List de String con los agentes a incluir en el sniffer.
	 * @throws Exception si algo falla
	 */
	private static void addSniffer(AgentContainer mc, List<String> agents) throws Exception {
		// Array de argumentos para el sniffer, contiene los nombres de los agentes sobre		
		agents.add("df");
		Object[] arguments = { String.join(";", agents) };
		AgentController sniffer = mc.createNewAgent("snifferAgent",Sniffer.class.getName(), arguments);
		sniffer.start();
		
	}
	
}
