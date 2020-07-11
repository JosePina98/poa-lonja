package es.um.poa.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import jade.core.Agent;
import jade.util.Logger;

/**
 * Clase que sirve para que los Agentes escriban mensaje en el log
 * 
 * @author Jose Antonio Pina Gomez
 * 
 */
public class AgentLoggerWrapper {	
	private Logger logger;
	private Agent agent;
	private String color;
	
	public AgentLoggerWrapper(Agent agent) {
		this.agent = agent;
		this.color = ColorManager.getColor(agent.getLocalName());
		this.logger = Logger.getMyLogger(agent.getClass().getCanonicalName());
	}
	
	public void info(String behaviour, String msg) {
		Object[] params = {this.agent.getLocalName(), behaviour, color};
		System.out.println(params[0]+","+params[1]+","+params[2]);
		this.logger.log(Level.INFO, msg, params);
	}
	
	public void close() {
		for(Handler handler: this.logger.getHandlers()) {
			if(handler instanceof FileHandler) {
				((FileHandler)handler).close();
			}
		}
	}
}

class ColorManager {
	private static Map<String,String> colorMapping = new HashMap<String, String>();
	private static String[] colors = {"#8BA900", "#0080A9", "#7600A9", "#0017A9",  "#DABF00", "#DA7700", "#DA1600", "#A9008B"};
	private static int index = -1;
	
	public static String getColor(String name) {
		String color = colorMapping.get(name);
		if(color == null) {
			index = (index + 1) % colors.length;
			color =  colors[index];
			colorMapping.put(name, color);
		}
		return color;
	}
}
