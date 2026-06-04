package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import factory.IFabricaEntidades;
import persistencia.ClienteDTO;
import persistencia.DAOFactory;
import persistencia.FabricaProductor;
import persistencia.PuestoDTO;
import seguridad.SeguridadFacade;
import util.Cliente;
import util.ConfigServidor;
import util.GestorJson;
import util.Protocolo;
import util.Puesto;

public class ServidorLogic {
    private ConfigServidor config;
    private IFabricaEntidades fabrica;
    private volatile boolean esRespaldo;
    private String nombreServidor;
    private final int puertoServidor;
    private final String ip;

    private LinkedList<Puesto> listaPuestosRegistrados = new LinkedList<>();
    private LinkedList<Cliente> colaClientesEnEspera = new LinkedList<>();
    private LinkedList<PrintWriter> monitoresConectados = new LinkedList<>();
    private LinkedList<String> ultimosLlamados = new LinkedList<>();
    private final int MAX_LLAMADOS_PANTALLA = 5;

    public ServidorLogic(ConfigServidor config, boolean esRespaldo, int puertoAsignado) {
        this.config = config;
        this.esRespaldo = esRespaldo;
        this.puertoServidor = puertoAsignado;
        this.fabrica = new factory.FabricaEntidadesConcreta();
        
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
        }else {
        	cargarEstadoDesdeDisco();
        	iniciarLimpiezaDePuestos();
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
                StringBuilder sb = new StringBuilder(Protocolo.MSG_SYNC_MONITOR);
                for (String llamado : ultimosLlamados) {
                    sb.append(Protocolo.SEPARADOR).append(llamado);
                }
                out.println(sb.toString());
                
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
	            case "CLON_PANTALLA":
	                String infoLlamado = partes[1] + Protocolo.SEPARADOR + partes[2];
	                ultimosLlamados.addLast(infoLlamado); 
	                guardarEstadoEnDisco();
	                break;
                case "CLON_CLIENTE": 
                    
                    String dniDescifrado = SeguridadFacade.descifrarDni(partes[1]);
                    colaClientesEnEspera.addLast(fabrica.crearCliente(dniDescifrado));
                    guardarEstadoEnDisco();
                    break;
                    
                case "CLON_PUESTO": 
                    String ip = partes[1];
                    String puerto = partes[2];
                    int nroPuesto = Integer.parseInt(partes[3]);
                    boolean activo = partes[4].equals("1");
                    
                    
                    String dniPuesto = partes[5];
                    if (!dniPuesto.equals("VACIO")) {
                        dniPuesto = SeguridadFacade.descifrarDni(dniPuesto);
                    }
                    
                    int reintentos = Integer.parseInt(partes[6]);               
                    listaPuestosRegistrados.add(fabrica.crearPuestoClonado(ip, puerto, dniPuesto, reintentos, nroPuesto, activo, System.currentTimeMillis()));
                    guardarEstadoEnDisco();
                    break;
                    
                case "CLON_ACTIVA_PUESTO": 
                    Puesto puestoActivar = buscarPuestoPorId(partes[1]); 
                    if (puestoActivar != null) {
                        puestoActivar.setActivo(true);
                    }
                    guardarEstadoEnDisco();
                    break;
                    
                case "CLON_LLAMAR":
                    Cliente clienteLlamado = colaClientesEnEspera.poll();
                    Puesto puestoLlamador = buscarPuestoPorId(partes[1]); 
                    if (puestoLlamador != null && clienteLlamado != null) {
                        System.out.println("Puesto " + puestoLlamador.getNroPuesto() + " asignado al cliente " + clienteLlamado.getDni() + " (replicado)");
                        puestoLlamador.asignarClienteAlPuesto(clienteLlamado);
                        actualizarPantallas(clienteLlamado.getDni(), partes[1]);
                    }
                    guardarEstadoEnDisco();
                    break;
                    
                case "CLON_DESCONECTAR":
                    Puesto puestoDesconectar = buscarPuestoPorId(partes[1]); 
                    if (puestoDesconectar != null) {
                        puestoDesconectar.setActivo(false);
                        guardarEstadoEnDisco();
                    }
                    break;
                    
                case "CLON_RELLAMAR":
                    Puesto p = buscarPuestoPorId(partes[1]); 
                    if (p != null && p.getReintentos() > 0) {
                        p.disminuirReintento();
                        actualizarPantallas(p.getDni(), partes[1]);
                        System.out.println(p.getNroPuesto() + " tiene " + p.getReintentos() + " reintentos antes de perder al cliente. soy respaldo");
                        guardarEstadoEnDisco();
                    }
                    break;
            }
            return "OK_CLON";
        }

        
        if (esRespaldo) {
            return Protocolo.ERR_CONEXION;
        }

        
        switch (comando) {
            case Protocolo.CMD_DESCONECTAR:
                Puesto puestoDesconectar = buscarPuestoPorId(partes[1]);
                if (puestoDesconectar != null) {
                    puestoDesconectar.setActivo(false);
                    guardarEstadoEnDisco();
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
                    puestoExistente.actualizarContacto();
                    guardarEstadoEnDisco();
                    replicarEnRespaldo("CLON_ACTIVA_PUESTO" + Protocolo.SEPARADOR + partes[2]);
                    return Protocolo.OK_REGISTRADO;
                }
                listaPuestosRegistrados.add(fabrica.crearPuesto(partes[1], partes[3], partes[2], true));
                String cadena = ("CLON_PUESTO" + Protocolo.SEPARADOR + partes[1] + Protocolo.SEPARADOR + partes[3] +
                                 Protocolo.SEPARADOR + partes[2] + Protocolo.SEPARADOR + "1" + Protocolo.SEPARADOR + "VACIO" + Protocolo.SEPARADOR + "0");
                guardarEstadoEnDisco();
                replicarEnRespaldo(cadena);
                return Protocolo.OK_REGISTRADO;
                
            case Protocolo.CMD_LLAMAR:
                Puesto puestoAsignar = buscarPuestoPorId(partes[1]);
                if (puestoAsignar != null)
                	puestoAsignar.actualizarContacto();
                Cliente clienteEnCola = colaClientesEnEspera.poll();
                
                if (puestoAsignar != null && clienteEnCola != null) {
                    puestoAsignar.asignarClienteAlPuesto(clienteEnCola); 
                    actualizarPantallas(clienteEnCola.getDni(), partes[1]);
                    guardarEstadoEnDisco();
                    replicarEnRespaldo("CLON_LLAMAR" + Protocolo.SEPARADOR + partes[1]);
                    
                    
                    String dniCifradoSaliente = SeguridadFacade.cifrarDni(clienteEnCola.getDni());
                    return "OK_LLAMAR" + Protocolo.SEPARADOR + dniCifradoSaliente;
                }
                return Protocolo.ERR_FILA_VACIA;
                
            case Protocolo.CMD_PEDIR_ESTADO:
                StringBuilder estadoComprimido = new StringBuilder();
                for (Puesto pu : listaPuestosRegistrados) {
                    String dniDelPuesto = pu.getDni();
                    
                    if (!dniDelPuesto.equals("VACIO")) {
                        dniDelPuesto = SeguridadFacade.cifrarDni(dniDelPuesto);
                    }
                    estadoComprimido.append("CLON_PUESTO").append(Protocolo.SEPARADOR)
                                    .append(pu.getIp()).append(Protocolo.SEPARADOR)
                                    .append(pu.getPuerto()).append(Protocolo.SEPARADOR)
                                    .append(pu.getNroPuesto()).append(Protocolo.SEPARADOR)
                                    .append(pu.isActivo() ? "1" : "0").append(Protocolo.SEPARADOR)
                                    .append(dniDelPuesto).append(Protocolo.SEPARADOR)
                                    .append(pu.getReintentos()).append(Protocolo.SEP_ESTADO);
                }
                for (Cliente cu : colaClientesEnEspera) {
                    String dniClienteFila = SeguridadFacade.cifrarDni(cu.getDni());
                    
                    estadoComprimido.append("CLON_CLIENTE").append(Protocolo.SEPARADOR)
                                    .append(dniClienteFila).append(Protocolo.SEP_ESTADO);
                }
                for (String llamado : ultimosLlamados) {
                    estadoComprimido.append("CLON_PANTALLA").append(Protocolo.SEPARADOR)
                                    .append(llamado).append(Protocolo.SEP_ESTADO);
                }
                if (estadoComprimido.length() > 0) {
                    return estadoComprimido.toString();
                } else {
                    return "VACIO";
                }
            case Protocolo.CMD_NUEVO_CLIENTE:
                
                String dniNuevoLimpio = SeguridadFacade.descifrarDni(partes[1]);
                return validaCliente(dniNuevoLimpio);
                
            case Protocolo.CMD_INFO_FILA:
                return String.valueOf(colaClientesEnEspera.size());
                
            case Protocolo.CMD_RELLAMAR:
            	Puesto pRellamar = buscarPuestoPorId(partes[1]);
                if (pRellamar != null)
                	pRellamar.actualizarContacto(); // <-- ACÁ
                return this.Rellamar(partes[1]);
 
                 
            default: 
                return Protocolo.ERR_COMANDO;
        }
    }
    
    public synchronized void anadirCliente(String dni) {
        colaClientesEnEspera.addLast(fabrica.crearCliente(dni));
        guardarEstadoEnDisco();
        String dniCifrado = SeguridadFacade.cifrarDni(dni);
        replicarEnRespaldo("CLON_CLIENTE" + Protocolo.SEPARADOR + dniCifrado);
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
        if (p.getReintentos() > 0) {
            p.disminuirReintento();
            actualizarPantallas(p.getDni(), nroPuesto);
            guardarEstadoEnDisco();
            replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            return Protocolo.OK_RELLAMADO;
        } else {
            replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            return Protocolo.SIN_REINTENTOS;
        }
    }

   
    
    private void descargarEstadoInicial() {
        
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
        String dniCifrado = SeguridadFacade.cifrarDni(dni);
        String infoLlamado = dniCifrado + Protocolo.SEPARADOR + "Puesto " + numPuesto;
        ultimosLlamados.removeIf(llamado -> llamado.startsWith(dniCifrado));
        ultimosLlamados.addFirst(infoLlamado);
        
        if (ultimosLlamados.size() > MAX_LLAMADOS_PANTALLA) {
            ultimosLlamados.removeLast();
        }
        StringBuilder sb = new StringBuilder(Protocolo.MSG_SYNC_MONITOR);
        for (String llamado : ultimosLlamados) {
            sb.append(Protocolo.SEPARADOR).append(llamado);
        }
        String mensajeFinal = sb.toString();
        synchronized(monitoresConectados) {
            Iterator<PrintWriter> iteradorMonitores = monitoresConectados.iterator();
            while (iteradorMonitores.hasNext()) { 
                try { 
                    iteradorMonitores.next().println(mensajeFinal); 
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
                    
                    
                    String[] principal = GestorJson.obtenerPrincipalActivo();
                    
                    if (principal != null) {
                        try (Socket s = new Socket(principal[0], Integer.parseInt(principal[1]))) {
                            
                        } catch (Exception e) {
                            
                            System.out.println("! >>> Principal caído detectado en: " + principal[1]);
                            GestorJson.marcarInactivo(principal[0], Integer.parseInt(principal[1]));
                            
                            
                            String[] nuevoHeredero = GestorJson.obtenerHeredero();
                            if (nuevoHeredero != null && puertoServidor == Integer.parseInt(nuevoHeredero[1])) {
                                esRespaldo = false; 
                                ServidorMain.setEsRespaldo(false);
                                GestorJson.registrarOActualizar(ip, puertoServidor, true, true);
                                actualizarIdentidad();
                                guardarEstadoEnDisco();
                                iniciarLimpiezaDePuestos();
                                System.out.println("! >>> ME HE CONVERTIDO EN EL NUEVO PRINCIPAL <<<");
                                System.out.println(colaClientesEnEspera);
                                System.out.println(listaPuestosRegistrados);
                                System.out.println(monitoresConectados);
                                break; 
                            }
                        }
                    } else {
                        
                        String[] nuevoHeredero = GestorJson.obtenerHeredero();
                        if (nuevoHeredero != null && puertoServidor == Integer.parseInt(nuevoHeredero[1])) {
                            esRespaldo = false;
                            ServidorMain.setEsRespaldo(false);
                            GestorJson.registrarOActualizar(ip, puertoServidor, true, true);
                            actualizarIdentidad();
                            iniciarLimpiezaDePuestos();
                            break;
                        }
                    }
                } catch (Exception ex) {
                    
                }
            }
        }).start();
    }
    private String obtenerNombreArchivoPersistencia() {
        return config.getArchivoPersistencia() + "_" + puertoServidor;
    }
    public void guardarEstadoEnDisco() {
        List<ClienteDTO> clientesDTO = new ArrayList<>();
        for (Cliente c : this.colaClientesEnEspera) {
            clientesDTO.add(new ClienteDTO(c.getDni()));
        }

        List<PuestoDTO> puestosDTO = new ArrayList<>();
        for (Puesto p : this.listaPuestosRegistrados) {
        	puestosDTO.add(new PuestoDTO(p.getIp(), p.getPuerto(), p.getDni(), p.getReintentos(), p.getNroPuesto(), p.isActivo(), p.getUltimoContacto()));
        }

        DAOFactory fabrica = FabricaProductor.obtenerFabrica(config.getFormatoPersistencia());
        String archivoBase = obtenerNombreArchivoPersistencia();

        fabrica.crearClienteDAO().guardarCola(clientesDTO, archivoBase);
        fabrica.crearPuestoDAO().guardarPuestos(puestosDTO, archivoBase);
        fabrica.crearPantallaDAO().guardarPantalla(this.ultimosLlamados, archivoBase);
        
        System.out.println("[Persistencia] Estado guardado exitosamente.");
    }

    public void cargarEstadoDesdeDisco() {
        DAOFactory fabrica = FabricaProductor.obtenerFabrica(config.getFormatoPersistencia());
        String archivoBase = obtenerNombreArchivoPersistencia();

        List<ClienteDTO> cli = fabrica.crearClienteDAO().leerCola(archivoBase);
        if (cli != null) {
            this.colaClientesEnEspera.clear();
            for (ClienteDTO c : cli) this.colaClientesEnEspera.addLast(this.fabrica.crearCliente(c.getDni()));
        }

        List<PuestoDTO> pue = fabrica.crearPuestoDAO().leerPuestos(archivoBase);
        if (pue != null) {
            this.listaPuestosRegistrados.clear();
            long tiempoActual = System.currentTimeMillis();

            for (PuestoDTO p : pue) {
                boolean estaActivo = p.isActivo();
                
                if (estaActivo && (tiempoActual - p.getUltimoContacto()) > 30000) {
                    estaActivo = false;
                }
                this.listaPuestosRegistrados.add(this.fabrica.crearPuestoClonado(
                        p.getIp(), 
                        p.getPuerto(), 
                        p.getDni(), 
                        p.getReintentos(), 
                        p.getNroPuesto(), 
                        estaActivo,
                        p.getUltimoContacto()
                    ));
            }
        }

        List<String> pan = fabrica.crearPantallaDAO().leerPantalla(archivoBase);
        if (pan != null) this.ultimosLlamados = new LinkedList<>(pan);

        System.out.println("[Persistencia] Estado cargado desde el disco.");
    }
    private void iniciarLimpiezaDePuestos() {
        new Thread(() -> {
            while (!esRespaldo) { // Solo el principal limpia
                try {
                    Thread.sleep(10000); 
                    long tiempoActual = System.currentTimeMillis();
                    boolean huboCambios = false;

                    for (Puesto p : listaPuestosRegistrados) {
                        if (p.isActivo() && (tiempoActual - p.getUltimoContacto()) > 30000) {
                            p.setActivo(false);
                            huboCambios = true;
                            System.out.println("[INFO] Puesto " + p.getNroPuesto() + " apagado por inactividad (Timeout).");
                            replicarEnRespaldo("CLON_DESCONECTAR" + Protocolo.SEPARADOR + p.getNroPuesto());
                        }
                    }
                    if (huboCambios) guardarEstadoEnDisco();
                } catch (Exception e) { break; }
            }
        }).start();
    }
}