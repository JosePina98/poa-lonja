package es.um.poa.agents.fishmarket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import es.um.poa.agents.POAAgent;
import es.um.poa.agents.seller.Lote;
import es.um.poa.utils.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

/**
 * Clase que representa al Agente Lonja
 * 
 * @author Jose Antonio Pina Gomez
 *
 */
public class FishMarketAgent extends POAAgent{
	
	private static final long serialVersionUID = 1L;
	private static final int LATENCIA = 5000;
	private static final int VENTANA_DE_OPORTUNIDAD = 1000;
	private static final float DECREMENTO_DE_PRECIO = 10.0F;
	private static final double TIEMPO_MAXIMO_SUBASTA = 60000;
	
	private HashMap<AID, Float> mapaCompradores; // Contiene el dinero de cada comprador
	private HashMap<AID, List<Lote>> mapaLotesCompradores; // Contiene los lotes que cada comprador ha comprado
	private HashMap<AID, List<Lote>> mapaVendedores; // Contiene los lotes que cada vendedor ha depositado pero no ha vendido
	private HashMap<AID, Float> mapaDineroVendedores; // Contiene el dinero de los lotes que cada vendedor ha vendido pero aun no ha cobrado
	private int tiempo = LATENCIA;
	private float ingresos_lonja = 0;
	private boolean activado = false;
	private long tiempoLatencia;
	
	/**
	 * Funcion que sirve para incializar al agente y para repartir el resto de roles 
	 * que habrá dentro de la lonja
	 * 
	 */
	public void setup() {
		super.setup();
		Object[] args = getArguments();
		String configFile = (String) args[0];
		FishMarketAgentConfig config = initAgentFromConfigFile(configFile);
		
		if (args != null && args.length == 1) {
			
			if(config != null) {
								
				mapaCompradores = new HashMap<>();
				mapaVendedores = new HashMap<>();
				mapaLotesCompradores = new HashMap<>();
				mapaDineroVendedores = new HashMap<>();

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
				
				// Inicializamos el rol de Receptor de Compradores (RRC)
				MessageTemplate protocolo_admision_comprador = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.ADMISION_COMPRADOR));
				addBehaviour(new ProtocoloAdmisionCompradorResponder(this, protocolo_admision_comprador));
				this.getLogger().info("INFO", "ProtocoloAdmisionComprador anadido con exito");
				
				// Inicializamos el rol de Admision de Vendedores (RAV)
				MessageTemplate protocolo_registro_vendedor = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.REGISTRO_VENDEDOR));
				addBehaviour(new ProtocoloRegistroVendedorResponder(this, protocolo_registro_vendedor));
				this.getLogger().info("INFO", "ProtocoloRegistroVendedor anadido con exito");
				
				// Inicializamos el rol de Gestor de Compras (RGC)
				MessageTemplate protocolo_apertura_credito = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.APERTURA_CREDITO));
				addBehaviour(new ProtocoloAperturaCreditoResponder(this, protocolo_apertura_credito));
				this.getLogger().info("INFO", "ProtocoloAperturaCredito anadido con exito");
				
				// Inicializamos el rol Receptor de Ventas (RRV)
				addBehaviour(new ProtocoloDepositoResponder());
				this.getLogger().info("INFO", "ProtocoloDeposito anadido con exito");
				
				// Inicializamos el rol de Terminacion de Compradores (RTC)
				MessageTemplate protocolo_terminacion_comprador = MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
						MessageTemplate.MatchConversationId(ConversationID.TERMINACION_COMPRADOR));
				addBehaviour(new ProtocoloTerminacionResponder(this, protocolo_terminacion_comprador));
				this.getLogger().info("INFO", "ProtocoloTerminacion anadido con exito");

				// Inicializamos el rol de Subasta (RS)
				addBehaviour(new ProtocoloSubastaInitiator());
				this.getLogger().info("INFO", "ProtocoloSubasta anadido con exito");
				
			} else {
				doDelete();
			}
		} else {
			this.getLogger().info("ERROR", "Requiere fichero de configuracion.");
			doDelete();
		}
	}
	
	/**
	 * Funcion que sirve para incializar al agente con los datos del fichero de configuracion.
	 * 
	 * @param fileName nombre del fichero de configuracion
 	 * @return devuelve un objeto FishMarketAgentConfig con los datos de configuracion
	 */
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
	
	/**
	 * Clase privada que implementa el protocolo-admision-comprador.
	 * 
	 * Este protocolo lo manejará el Agente con el Rol Receptor de Compradores (RRC).
	 * 
	 * Los compradores inicializaran este protocolo para ser admitidos en la lonja.
	 *
	 */
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
			if (mapaCompradores.put(request.getSender(), 0F) == null &&
					mapaLotesCompradores.put(request.getSender(), new LinkedList<>()) == null) 
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
	
	/**
	 * Clase privada que implementa el protocolo-registro-vendedor.
	 * 
	 * Este protocolo lo manejara el agente con Rol Admision de Vendedores (RAV).
	 * 
	 * Los vendedores inicializaran este protocolo para registrarse en la Lonja.
	 *  
	 */
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
			if (mapaVendedores.put(request.getSender(), new LinkedList<Lote>()) == null
					&& mapaDineroVendedores.put(request.getSender(), 0F) == null) 
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
	
	/**
	 * Clase privada que implementa el protocolo-apertura-credito.
	 * 
	 * Este protocolo lo manejara el agente con Rol Gestor de Compras (RGC).
	 * 
	 * Los compradores inicializaran este protocolo para abrir su linea de credito.
	 * 
	 */
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
			if (mapaCompradores.containsKey(request.getSender()) && mapaCompradores.get(request.getSender()) == 0) {
	
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
	
	/**
	 * Clase privada que implementa el protocolo-deposito.
	 * 
	 * Este protocolo lo manejara el agente con Rol Receptor de Ventas (RRV).
	 * 
	 * Los vendedores inicializaran este protocolo para ir depositando sus lotes para ser subastados.
	 * 
	 */
	@SuppressWarnings("serial")
	private class ProtocoloDepositoResponder extends CyclicBehaviour {

		@Override
		public void action() {

			MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId(ConversationID.DEPOSITO_CAPTURA), 
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			
			ACLMessage request = myAgent.receive(template);
			Lote lote = null;
			
			if (request != null) {
				getLogger().info("ProtocoloDeposito - Responder", "El Vendedor \"" + request.getSender().getLocalName() + "\" quiere depositar sus capturas");
				
				ACLMessage respuesta = request.createReply();
				respuesta.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				try {
					lote = (Lote) request.getContentObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (mapaVendedores.containsKey(request.getSender()) && lote != null) {
					List<Lote> aux = mapaVendedores.get(request.getSender());
					
					aux.add(lote);
					
					// Iniciamos la busqueda para abrir subastas
					activado = true;
					tiempoLatencia = System.currentTimeMillis() + LATENCIA;

					respuesta.setPerformative(ACLMessage.INFORM);
				} else {
					respuesta.setPerformative(ACLMessage.REFUSE);
				}
				
				myAgent.send(respuesta);
			} else {
				block();
			}
		}
		
	}
	
	/**
	 * Clase privada que implementa el protocolo-subasta.
	 * 
	 * Este protocolo lo manejara el agente con Rol Subasta (RS).
	 * 
	 * Cuando la lonja tenga lotes preparadores para vender y compradores preparados para comprar
	 * podrá iniciar la subasta de lotes. Si no se puede iniciar una subasta, espera 'tiempoLatencia' 
	 * milisegundos para intentarlo de nuevo.
	 * 
	 */
	@SuppressWarnings("serial")
	private class ProtocoloSubastaInitiator extends CyclicBehaviour {
		
		private Lote lote;
		private AID vendedor;
		private List<AID> listaPosiblesCompradores;
		private int step = 0;
		private long tiempoInicioPropuesta;
		private MessageTemplate template;
		private boolean nuevaSubasta = true;
		
		@Override
		public void action() {
			if (!activado || System.currentTimeMillis() < tiempoLatencia) return;
			
			switch (step) {
			case 0: // No hay ninguna puja en marcha
				
				getLogger().info("ProtocoloSubasta - Initiator", "La Lonja esta comprobando si se puede iniciar una nueva subasta. Ganancias hasta ahora: " + ingresos_lonja + "e");

				
				// Inicializamos todas las variables
				lote = null;
				vendedor = null;
				listaPosiblesCompradores = new LinkedList<AID>();
				tiempoInicioPropuesta = 0;
				template = null;
				
				// Comprobar que tenemos un lote valido para subastar
				for (AID aid : mapaVendedores.keySet()) {
					List<Lote> pilaLotes = mapaVendedores.get(aid);
					
					if (!pilaLotes.isEmpty()) {
						lote = pilaLotes.get(0);
						vendedor = aid;
						break;
					}

				}
				
				if (lote != null && vendedor != null) {
					// Comprobar que hay vendedores registrados y con la linea de credito abierta
					for (AID aid : mapaCompradores.keySet()) {
						if (mapaCompradores.get(aid) > lote.getPrecioReserva()) {
							listaPosiblesCompradores.add(aid);
						}
					}
				}
				
				if (listaPosiblesCompradores.size() > 0) {
					step = 1; // Podemos arrancar una puja
					nuevaSubasta = true;
				} else {
					tiempoLatencia = System.currentTimeMillis() + LATENCIA;
					break;
				}
				
				break;
				
			case 1: // Enviamos una propuesta de precio a los posibles compradores
				
				if (nuevaSubasta) {
					getLogger().info("ProtocoloSubasta - Initiator", "NUEVA SUBASTA: " + vendedor.getLocalName() + " vende "
							+ lote.getKg() + "Kg de " + lote.getTipo() + ". La subasta empieza en: "
							+ lote.getPrecioSalida() + "e y el precio minimo es de " + lote.getPrecioReserva() + "e");
				
					nuevaSubasta = false;
				} else {
					getLogger().info("ProtocoloSubasta - Initiator", "Bajada de precio: " + vendedor.getLocalName() + " vende "
							+ lote.getKg() + "Kg de " + lote.getTipo() + ". El precio ahora es " + lote.getPrecioActual() + "e");
				}
				// Enviar un mensaje PROPOSE a cada comprador que tenga linea de credito abierta con mas dinero que el precio de reserva
				ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
				propose.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
				propose.setConversationId(ConversationID.SUBASTA);
				propose.setReplyWith("propose" + System.currentTimeMillis());
				try {
					propose.setContentObject(lote);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Actualizamos la lista de posibles compradores por si alguno se ha dado de baja
				listaPosiblesCompradores.clear();
				for (AID aid : mapaCompradores.keySet()) {
					if (mapaCompradores.get(aid) > lote.getPrecioReserva()) {
						listaPosiblesCompradores.add(aid);
					}
				}
				
				String mensajeLog = "Enviamos un Propose a: ";
				boolean primeraIt = true;
				for (AID posibleComprador : listaPosiblesCompradores) {
					if (primeraIt) {
						mensajeLog += posibleComprador.getLocalName();
						primeraIt = false;
					} else {
						mensajeLog += ", " + posibleComprador.getLocalName();
					}

					propose.addReceiver(posibleComprador);
				}
				mensajeLog += " para ver si pujan por " + lote.getPrecioActual() + "e";
				getLogger().info("ProtocoloSubasta - Initiator", mensajeLog);
				myAgent.send(propose);
				
				MessageTemplate template1 = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL), 
						MessageTemplate.MatchConversationId(ConversationID.PUJAR));
				
				template = MessageTemplate.and(template1, 
						MessageTemplate.MatchInReplyTo(propose.getReplyWith()));

				tiempo = VENTANA_DE_OPORTUNIDAD;
				tiempoInicioPropuesta = System.currentTimeMillis();
				step = 2;
				break;
				
			case 2: // Esperamos respuestas de los posibles compradores
				
				if (System.currentTimeMillis() - tiempoInicioPropuesta >= TIEMPO_MAXIMO_SUBASTA) {
					// Se ha cumplido el tiempo maximo de la subasta, subasta terminada
					getLogger().info("ProtocoloSubasta - Initiator", "SUBASTA TERMINADA - Se ha cumplido el tiempo maximo de subasta, el lote sera descartado");

					mapaVendedores.get(vendedor).remove(0); // Borramos el lote del vendedor
					step = 0;
					break;
				}
				
				ACLMessage respuesta = myAgent.receive(template);
				
				if (respuesta != null) {
					// Hemos recibido una puja
					if (mapaCompradores.get(respuesta.getSender()) >= lote.getPrecioActual()) {
						// El comprador tiene dinero suficiente para pagar la puja
						getLogger().info("ProtocoloSubasta - Initiator", "GANADOR DE LA SUBASTA: " + respuesta.getSender().getLocalName()
								+ " se lleva " + lote.getKg() + " Kg de " + lote.getTipo() + " por " + lote.getPrecioActual() + "e");

						// PUJA CORRECTA - RESPONDER CON UN INFORM Y ACTUALIZAR LA BBDD - LA PUJA HA ACABADO, VOLVER AL PASO 0
						ACLMessage mensaje = respuesta.createReply();
						mensaje.setPerformative(ACLMessage.INFORM);
						try {
							mensaje.setContentObject(lote);
						} catch (IOException e) {
							e.printStackTrace();
						}
						myAgent.send(mensaje);

						mapaVendedores.get(vendedor).remove(0); // Borramos el lote del vendedor

						// Esto es el equivalente al protocolo_retirada_compras
						mapaCompradores.put(respuesta.getSender(), mapaCompradores.get(respuesta.getSender()) - lote.getPrecioActual()); // Actualizamos el dinero del comprador
						mapaLotesCompradores.get(respuesta.getSender()).add(lote); // Añadimos el lote comprado a su lista de lotes comprados
						
						ingresos_lonja += lote.getPrecioActual(); // Actualizamos nuestros ingresos
						
						mapaDineroVendedores.put(vendedor, mapaDineroVendedores.get(vendedor) + lote.getPrecioActual()); // Actualizamos el dinero del vendedor sin retirar

						// Inicializamos el rol Gestor de Ventas (RGV)
						ACLMessage aux = new ACLMessage(ACLMessage.REQUEST);
						aux.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
						aux.addReceiver(vendedor);
						aux.setConversationId(ConversationID.COBRO_VENDEDOR);
						aux.setContent(String.valueOf(lote.getPrecioActual()));
						addBehaviour(new ProtocoloCobroInitiator(myAgent, aux));
						
						step = 0;
						
						break;
						
					} else {
						// Puja incorrecta, el comprador no tiene suficiente dinero
						ACLMessage mensaje = respuesta.createReply();
						mensaje.setPerformative(ACLMessage.REFUSE);
						try {
							mensaje.setContentObject(lote);
						} catch (IOException e) {
							e.printStackTrace();
						}
						myAgent.send(mensaje);
						getLogger().info("ProtocoloSubasta - Initiator", "El Comprador \"" + respuesta.getSender().getLocalName() + "\" ha intentado pujar por " + lote.getPrecioActual() + ", pero no tiene suficiente dinero");
					}
				} else {
					// O no hemos recibido nada, o lo que hemos recibido no entra en la plantilla (por ejemplo, un reject)
					
					if (System.currentTimeMillis() - tiempoInicioPropuesta >= tiempo) {
						// Ha pasado el tiempo de Ventana de Oportunidad, hay que bajar el precio de la puja
						float nuevoPrecio = lote.getPrecioActual() - DECREMENTO_DE_PRECIO;
						if (nuevoPrecio < lote.getPrecioReserva()) {
							getLogger().info("ProtocoloSubasta - Initiator", "SUBASTA TERMINADA - El precio de la puja ha bajado del precio de reserva, se descarta el lote");

							// Se ha bajado del precio de reserva, no se puede vender el lote
							lote.setPrecioActual(lote.getPrecioReserva());
							
							// PUJA TERMINADA - ACTUALIZAR BDD Y VOLVER AL PASO 0
							mapaVendedores.get(vendedor).remove(0); // Borramos el lote del vendedor
							step = 0;
							break;
							
						} else {
							lote.setPrecioActual(nuevoPrecio);
							step = 1;
						}
					} else {
						block(VENTANA_DE_OPORTUNIDAD / 3); // Lo bloqueamos un tercio del tiempo de la ventana de oportunidad
					}
				}
				
			}
						
		}
	}
	
	/**
	 * Clase privada que implementa el protocolo-terminacion.
	 * 
	 * Este protocolo lo manejara el agente con el Rol Terminacion Compradores (RTC).
	 * 
	 * Con este protocolo manejaremos la terminacion de compradores, dandolos de baja del sistema
	 * para no incluirlos en futuras subastas.
	 * 
	 */
	@SuppressWarnings("serial")
	private class ProtocoloTerminacionResponder extends AchieveREResponder {

		public ProtocoloTerminacionResponder(Agent a, MessageTemplate mt) {
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
	
			getLogger().info("ProtocoloTerminacion - Responder", "El Comprador \"" + request.getSender().getLocalName() + "\" quiere abandonar la lonja");
			// Comprobamos si podemos llevar a cabo la peticion.
			if (mapaCompradores.containsKey(request.getSender())) {
	
				return null;
			}
			else {
				// Rechazamos a llevar a cabo la peticion.
				getLogger().info("ProtocoloTerminacion - Responder", "El Comprador \"" + request.getSender().getLocalName() + "\" no esta registrado");
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
			if (mapaCompradores.remove(request.getSender()) != null) {
				// La peticion se ha realizado con exito.
				getLogger().info("ProtocoloTerminacion - Responder", "Hemos dado de baja al Comprador \"" + request.getSender().getLocalName() + "\"");

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
	
	/**
	 * Clase privada que implementa el protocolo-cobro.
	 * 
	 * Este protocolo lo manejara el agente con Rol Gestor de Ventas (RGV).
	 * 
	 * Con este protocolo ofrecemos la posibilidad a un vendedor de cobrar el dinero obtenido
	 * por la venta de sus lotes.
	 * 
	 */
	@SuppressWarnings("serial")
	private class ProtocoloCobroInitiator extends AchieveREInitiator {

		public ProtocoloCobroInitiator(Agent a, ACLMessage msg) {
			super(a, msg);
			@SuppressWarnings("unchecked")
			Iterator<AID> it = msg.getAllReceiver();
			if (it.hasNext())
				getLogger().info("ProtocoloCobro - Initiator", "Se le ofrece la posibilidad de cobrar al Vendedor \"" + it.next().getLocalName() + "\"");
		}
		
		/**
		 * Maneja los mensajes inform recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleInform(ACLMessage msg) {
			super.handleInform(msg);
			getLogger().info("ProtocoloCobro - Initiator", "El Vendedor \"" + msg.getSender().getLocalName() + "\" ha aceptado el cobro");
			mapaDineroVendedores.put(msg.getSender(), 0F);
		}
		/**
		 * Maneja los mensajes failure recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleFailure(ACLMessage msg) {
			super.handleFailure(msg);
			getLogger().info("ProtocoloCobro - Initiator", "El Vendedor \"" + msg.getSender().getLocalName() + "\" ha rechazado el cobro");
		}
		/**
		 * Maneja los mensajes refuse recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleRefuse(ACLMessage msg) {
			super.handleRefuse(msg);
			getLogger().info("ProtocoloCobro - Initiator", "REFUSE recibido de \"" + msg.getSender().getLocalName() + "\", cobro denegado");
		}
		/**
		 * Maneja los mensajes notUnderstood recibidos.
		 * Se llama al padre y se añaden ordenes para la depuracion.
		 */
		@Override
		protected void handleNotUnderstood(ACLMessage msg) {
			super.handleNotUnderstood(msg);
			getLogger().info("ProtocoloCobro - Initiator", "NOTUNDERSTOOD recibido de \"" + msg.getSender().getLocalName() + "\"");
		}
	}
	
}
