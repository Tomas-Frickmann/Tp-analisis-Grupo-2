package servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import util.*;

public class ServidorLogic {
    private ConfigServidor config;
    private volatile boolean esRespaldo;
    private String nombreServidor;
    private final int puertoServidor;
    private final String ip;

    private LinkedList<Puesto> listaPuestosRegistrados = new LinkedList<>();
    private LinkedList<Cliente> colaClientesEnEspera = new LinkedList<>();
    private LinkedList<PrintWriter> monitoresConectados = new LinkedList<>();

    public ServidorLogic(ConfigServidor config, boolean esRespaldo, int puertoAsignado) {
        this.config = config;
        this.esRespaldo = esRespaldo;
        this.puertoServidor = puertoAsignado;
        
        if (puertoAsignado == config.getPuertoPrincipal()) {
            this.ip = config.getIpPrincipal();
        } else {
            this.ip = config.getIpRespaldo();
        }
        
        actualizarIdentidad();
    }

    private void actualizarIdentidad() {
        if (!esRespaldo) {
            this.nombreServidor = "Servidor PRINCIPAL (" + puertoServidor + ")";
        } else {
            this.nombreServidor = "Servidor RESPALDO (" + puertoServidor + ")";
        }
    }

    public void iniciarServidor() {
        abrirPuertoParaAtencion();
        if (esRespaldo) {
            descargarEstadoInicial();
            iniciarVigilancia();
        }
        System.out.println(">>> " + nombreServidor + " LISTO <<<");
    }

    private void replicarEnRespaldo(String comando) {
        if (!esRespaldo) {
            new Thread(() -> {
                List<String[]> respaldos = GestorJson.obtenerRespaldosActivos();
                for (String[] res : respaldos) {
                    try (Socket s = new Socket(res[0], Integer.parseInt(res[1]));
                         PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                        out.println(comando);
                    } catch (Exception e) {
                        
                    }
                }
            }).start();
        }
    }

    private void abrirPuertoParaAtencion() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puertoServidor)) {
                while (true) {
                    Socket s = ss.accept();
                    new Thread(() -> manejarPeticion(s)).start();
                }
            } catch (IOException e) {
                System.err.println("Error fatal en puerto de atención.");
            }
        }).start();
    }

    private void manejarPeticion(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            
            String msg = in.readLine();
            if (msg == null) {
                return;
            }
            
            String[] partes = msg.split(Protocolo.SEPARADOR);
            
            if (partes[0].equals(Protocolo.CMD_REGISTRO_MONITOR)) {
                synchronized(monitoresConectados) { 
                    monitoresConectados.add(out); 
                }
                while (s.isConnected() && !out.checkError()) { 
                    Thread.sleep(1000); 
                }
            } else {
                out.println(procesarAccion(partes));
            }
        } catch (Exception e) {
            
        }
    }

    private synchronized String procesarAccion(String[] partes) {
        String comando = partes[0];

       
        if (esRespaldo && comando.startsWith("CLON_")) {
            switch (comando) {
                case "CLON_CLIENTE": 
                    colaClientesEnEspera.addLast(new Cliente(partes[1])); 
                    break;
                    
                case "CLON_PUESTO": 
                	String ip= partes[1];
                	String puerto= partes[2];
                	int nroPuesto= Integer.parseInt(partes[3]);
                	boolean activo= partes[4].equals("1");
                	String dni= partes[5];
                	int reintentos = Integer.parseInt(partes[6]);           	
                	
                    listaPuestosRegistrados.add(new Puesto( ip,  puerto,  dni,  reintentos,  nroPuesto,  activo));
                	
                    break;
                    
                case "CLON_ACTIVA_PUESTO": 
                    Puesto puestoActivar = buscarPuestoPorId(partes[1]); 
                    if (puestoActivar != null) {
                        puestoActivar.setActivo(true);
                    }
                    break;
                    
                case "CLON_LLAMAR":
                    Cliente clienteLlamado = colaClientesEnEspera.poll();
                    Puesto puestoLlamador = buscarPuestoPorId(partes[1]); 
                    if (puestoLlamador != null && clienteLlamado != null) {
                    	System.out.println("Puesto " + puestoLlamador.getNroPuesto() + " asignado al cliente " + clienteLlamado.getDni() + " (replicado)");
                        puestoLlamador.asignarClienteAlPuesto(clienteLlamado);
                        System.out.println(clienteLlamado.getDni()+puestoLlamador.getReintentos());
                        
                    }
                    break;
                    
                case "CLON_DESCONECTAR":
                    Puesto puestoDesconectar = buscarPuestoPorId(partes[1]); 
                    if (puestoDesconectar != null) {
                        puestoDesconectar.setActivo(false);
                    }
                    break;
                case "CLON_RELLAMAR":
                	
                    Puesto p = buscarPuestoPorId(partes[1]); 
                    System.out.println(p.getNroPuesto() + " tiene " + p.getReintentos() + " reintentos antes de perder al cliente. soy respaldo");
                  
                   if (p.getReintentos()>0) {
                   p.disminuirReintento();
                   System.out.println(p.getNroPuesto() + " tiene " + p.getReintentos() + " reintentos antes de perder al cliente. soy respaldo");
                   }
                   
						
                    
           
                    break;
            }
            return "OK_CLON";
            
        }
        

        // Si soy respaldo pero el comando no es un CLON, rechazo la petición
        if (esRespaldo) {
            return Protocolo.ERR_CONEXION;
        }

       
        switch (comando) {
        case Protocolo.CMD_DESCONECTAR:
				Puesto puestoDesconectar = buscarPuestoPorId(partes[1]);
				if (puestoDesconectar != null) {
					puestoDesconectar.setActivo(false);
					replicarEnRespaldo("CLON_DESCONECTAR" + Protocolo.SEPARADOR + partes[1]);
					return Protocolo.OK_DESCONECTAR;
				}
				
				return Protocolo.ERR_PUESTO_NO_EXISTE;
            case Protocolo.CMD_REGISTRO:
                Puesto puestoExistente = buscarPuestoPorId(partes[2]);
                if (puestoExistente != null) {
                    if (puestoExistente.isActivo()) {
                        return Protocolo.ERR_PUESTO_EXISTE;
                    }
                    puestoExistente.setActivo(true); 
                    puestoExistente.setIp(partes[1]); 
                    puestoExistente.setPuerto(partes[3]);
                    replicarEnRespaldo("CLON_ACTIVA_PUESTO" + Protocolo.SEPARADOR + partes[2]);
                    return Protocolo.OK_REGISTRADO;
                }
                listaPuestosRegistrados.add(new Puesto(partes[1], partes[3], partes[2], true));
               String  cadena=("CLON_PUESTO"+Protocolo.SEPARADOR+partes[1]+Protocolo.SEPARADOR+ partes[3]+
            		   Protocolo.SEPARADOR+partes[2]+Protocolo.SEPARADOR+"1"+Protocolo.SEPARADOR+"VACIO"+Protocolo.SEPARADOR+"0");
                
                replicarEnRespaldo(cadena);
                return Protocolo.OK_REGISTRADO;
                
                
            case Protocolo.CMD_LLAMAR:
                Puesto puestoAsignar = buscarPuestoPorId(partes[1]);
                Cliente clienteEnCola = colaClientesEnEspera.poll();
                
                if (puestoAsignar != null && clienteEnCola != null) {
                    puestoAsignar.asignarClienteAlPuesto(clienteEnCola); 
                    actualizarPantallas(clienteEnCola.getDni(), partes[1]);
                    replicarEnRespaldo("CLON_LLAMAR" + Protocolo.SEPARADOR + partes[1]);
                    
                    return clienteEnCola.getDni();
                }
                return Protocolo.ERR_FILA_VACIA;
                
                
            case Protocolo.CMD_PEDIR_ESTADO:
                StringBuilder estadoComprimido = new StringBuilder();
                
                for (Puesto pu : listaPuestosRegistrados) {
                    estadoComprimido.append("CLON_PUESTO").append(Protocolo.SEPARADOR)
                                    .append(pu.getIp()).append(Protocolo.SEPARADOR)
                                    .append(pu.getPuerto()).append(Protocolo.SEPARADOR)
                                    .append(pu.getNroPuesto()).append(Protocolo.SEPARADOR)
                                    .append(pu.isActivo() ? "1" : "0").append(Protocolo.SEPARADOR)
                                    .append(pu.getDni()).append(Protocolo.SEPARADOR)
                                    .append(pu.getReintentos ()).append(Protocolo.SEP_ESTADO);
                                    
                }
            	
            	
           
            	
                
                for (Cliente cu : colaClientesEnEspera) {
                    estadoComprimido.append("CLON_CLIENTE").append(Protocolo.SEPARADOR)
                                    .append(cu.getDni()).append(Protocolo.SEP_ESTADO);
                }
                
                if (estadoComprimido.length() > 0) {
                    return estadoComprimido.toString();
                } else {
                    return "VACIO";
                }
                
            case Protocolo.CMD_NUEVO_CLIENTE:
            	
            	
                
               
                return validaCliente(partes[1]);
            case Protocolo.CMD_INFO_FILA:
				return String.valueOf(colaClientesEnEspera.size());
				
            case Protocolo.CMD_RELLAMAR:
            	 return this.Rellamar(partes[1]);
				
				
                
            default: 
                return Protocolo.ERR_COMANDO;
        }
    }
    
    public synchronized void anadirCliente(String dni) {
        colaClientesEnEspera.addLast(new Cliente(dni));
         replicarEnRespaldo("CLON_CLIENTE" + Protocolo.SEPARADOR + dni);
        System.out.println("Servidor: Cliente " + dni + " añadido a la fila normal.");
    }
    public synchronized String validaCliente(String dni) {
        for (Cliente aux : colaClientesEnEspera) {
            if (aux.getDni().equals(dni)) return Protocolo.ERR_DNI_DUPLICADO; 
        }
        anadirCliente(dni);
        return Protocolo.OK_CLIENTE_CREADO;
    }
    public synchronized String Rellamar(String nroPuesto) {
        
        try { Thread.sleep(500); } catch (Exception e) {}
        Puesto p = buscarPuestoPorId(nroPuesto); 
        if (p == null) return Protocolo.ERR_PUESTO_NO_EXISTE;        
        if (p.getReintentos()>0) {
        	p.disminuirReintento();
        	actualizarPantallas(p.getDni(), nroPuesto);
        	
        	replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
        	System.out.println(p.getNroPuesto() + " tiene " + p.getReintentos() + " reintentos antes de perder al cliente. soy principal");
        	
        	return p.getDni(); 
        	}
        else {
        	replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
        	return Protocolo.SIN_REINTENTOS;
        }}
    private void descargarEstadoInicial() {
        // Buscamos quién es el principal actual en el JSON, no en el config
        String[] principalActual = GestorJson.obtenerPrincipalActivo();
        
        if (principalActual == null) {
            System.out.println("Aviso: No hay un principal activo para sincronizar. Iniciando vacío.");
            return;
        }

        try (Socket s = new Socket(principalActual[0], Integer.parseInt(principalActual[1]));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            System.out.println("Sincronizando estado con el principal: " + principalActual[0] + ":" + principalActual[1]);
            out.println(Protocolo.CMD_PEDIR_ESTADO);
            String respuestaServidor = in.readLine();
            
            if (respuestaServidor != null && !respuestaServidor.equals("VACIO")) {
                String[] comandosClon = respuestaServidor.split(Protocolo.SEP_ESTADO);
                for (String comandoUnico : comandosClon) { 
                    if (!comandoUnico.isEmpty()) {
                        procesarAccion(comandoUnico.split(Protocolo.SEPARADOR)); 
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: No se pudo conectar al principal (" + principalActual[1] + ") para sincronizar.");
        }
    }
    private void actualizarPantallas(String dni, String numPuesto) {
        String mensaje = Protocolo.MSG_ACTUALIZAR_MONITOR + Protocolo.SEPARADOR + dni + Protocolo.SEPARADOR + "Puesto " + numPuesto;
        
        synchronized(monitoresConectados) {
            Iterator<PrintWriter> iteradorMonitores = monitoresConectados.iterator();
            while (iteradorMonitores.hasNext()) { 
                try { 
                    iteradorMonitores.next().println(mensaje); 
                } catch (Exception e) { 
                    iteradorMonitores.remove(); 
                } 
            }
        }
    }

    private Puesto buscarPuestoPorId(String id) {
        for (Puesto p : listaPuestosRegistrados) { 
            if (String.valueOf(p.getNroPuesto()).equals(id)) {
                return p;
            }
        }
        return null;
    }

    private void iniciarVigilancia() {
        new Thread(() -> {
            while (esRespaldo) {
                try {
                    Thread.sleep(config.getintervaloPing());
                    
                    // Consultamos al JSON quién es el principal que debemos vigilar HOY
                    String[] principal = GestorJson.obtenerPrincipalActivo();
                    
                    if (principal != null) {
                        try (Socket s = new Socket(principal[0], Integer.parseInt(principal[1]))) {
                            // El principal está vivo, no hacemos nada
                        } catch (Exception e) {
                            // Si falla la conexión, lo marcamos como muerto en el JSON
                            System.out.println("! >>> Principal caído detectado en: " + principal[1]);
                            GestorJson.marcarInactivo(principal[0], Integer.parseInt(principal[1]));
                            
                            // Verificamos si me toca a mí ser el nuevo líder
                            String[] nuevoHeredero = GestorJson.obtenerHeredero();
                            if (nuevoHeredero != null && puertoServidor == Integer.parseInt(nuevoHeredero[1])) {
                                esRespaldo = false; 
                                ServidorMain.setEsRespaldo(false);
                                GestorJson.registrarOActualizar(ip, puertoServidor, true, true);
                                actualizarIdentidad();
                                System.out.println("! >>> ME HE CONVERTIDO EN EL NUEVO PRINCIPAL <<<");
                                System.out.println(colaClientesEnEspera);
                                System.out.println(listaPuestosRegistrados);
                                System.out.println(monitoresConectados);
                                break; // Rompemos el bucle de vigilancia
                            }
                        }
                    } else {
                        // Si no hay principal en el JSON (caso raro), intentamos postularnos
                        String[] nuevoHeredero = GestorJson.obtenerHeredero();
                        if (nuevoHeredero != null && puertoServidor == Integer.parseInt(nuevoHeredero[1])) {
                            esRespaldo = false;
                            ServidorMain.setEsRespaldo(false);
                            GestorJson.registrarOActualizar(ip, puertoServidor, true, true);
                            actualizarIdentidad();
                            break;
                        }
                    }
                } catch (Exception ex) {
                    // Error silencioso en el hilo para evitar crashes
                }
            }
        }).start();
    }
}