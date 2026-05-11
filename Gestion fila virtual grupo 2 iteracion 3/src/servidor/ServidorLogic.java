package servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import util.ConfigServidor;
import util.Protocolo;
import util.GestorJson;

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
        this.ip = (puertoAsignado == config.getPuertoPrincipal()) ? config.getIpPrincipal() : config.getIpRespaldo();
        actualizarIdentidad();
    }

    private void actualizarIdentidad() {
        if (!esRespaldo) {
            this.nombreServidor = "Servidor PRINCIPAL (" + puertoServidor + ")";
        } else {
            this.nombreServidor = "Servidor RESPALDO (" + puertoServidor + ")";
        }
    }

    private void replicarEnRespaldo(String comandoOculto) {
        if (!esRespaldo) {
            new Thread(() -> {
                // El principal intenta enviar el clon al puerto de respaldo configurado
                try (Socket s = new Socket(config.getIpRespaldo(), config.getPuertoRespaldo());
                     PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                    out.println(comandoOculto);
                } catch (Exception e) { /* Respaldo offline */ }
            }).start();
        }
    }

    public void iniciarServidor() {
        abrirPuertoParaAtencion();
        if (esRespaldo) {
            iniciarVigilancia();
        }
        System.out.println(">>> " + nombreServidor + " LISTO <<<");
    }

    private void iniciarVigilancia() {
        new Thread(() -> {
            int fallos = 0;
            while (this.esRespaldo) {
                try {
                    Thread.sleep(config.getintervaloPing());
                    // Intento de conexión física con el Principal
                    try (Socket s = new Socket(config.getIpPrincipal(), config.getPuertoPrincipal())) {
                        fallos = 0; 
                    } catch (Exception e) {
                        fallos++;
                        if (fallos >= config.getMaxIntentosFallidos()) {
                            System.out.println("[VIGILANCIA] Principal inalcanzable. Forzando limpieza en JSON...");
                            
                            // Forzamos que el principal figure como muerto en el JSON
                            GestorJson.marcarInactivo(config.getIpPrincipal(), config.getPuertoPrincipal());

                            // ¿Hay algún otro líder que haya subido antes?
                            String[] nuevoLider = GestorJson.obtenerPrincipalActivo();
                            if (nuevoLider != null) {
                                config.setIpPrincipal(nuevoLider[0]);
                                config.setPuertoPrincipal(Integer.parseInt(nuevoLider[1]));
                                fallos = 0;
                                continue;
                            }

                            // Si no hay líder, ¿me toca a mí ser el heredero?
                            String[] heredero = GestorJson.obtenerHeredero();
                            if (heredero != null && this.puertoServidor == Integer.parseInt(heredero[1])) {
                                this.esRespaldo = false;
                                ServidorMain.setEsRespaldo(false);
                                GestorJson.registrarOActualizar(this.ip, this.puertoServidor, true, true);
                                actualizarIdentidad();
                                System.out.println(">>> [SISTEMA] ASCENSO EXITOSO a Principal.");
                                break; 
                            }
                        }
                    }
                } catch (Exception ie) { }
            }
        }).start();
    }

    private void abrirPuertoParaAtencion() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puertoServidor)) {
                while (true) {
                    Socket s = ss.accept();
                    new Thread(() -> manejarPeticion(s)).start();
                }
            } catch (Exception e) {
                System.err.println("Error fatal en puerto " + puertoServidor + ": " + e.getMessage());
            }
        }).start();
    }

    private void manejarPeticion(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            String mensaje = in.readLine();
            if (mensaje != null) {
                String[] partes = mensaje.split(Protocolo.SEPARADOR);
                if (partes[0].equals(Protocolo.CMD_REGISTRO_MONITOR)) {
                    monitoresConectados.add(out);
                    while (s.isConnected() && !out.checkError()) { Thread.sleep(1000); }
                } else {
                    out.println(procesarAccion(partes));
                }
            }
        } catch (Exception e) {}
    }

    private String procesarAccion(String[] partes) {
        String comando = partes[0];
        
        // Lógica de Sincronización (Clones)
        if (esRespaldo && comando.startsWith("CLON_")) {
            switch (comando) {
                case "CLON_CLIENTE": colaClientesEnEspera.addLast(new Cliente(partes[1])); break;
                case "CLON_PUESTO": listaPuestosRegistrados.add(new Puesto(partes[1], partes[2], partes[3], true)); break;
                case "CLON_ACTIVA_PUESTO": 
                    Puesto pAct = buscarPuestoPorId(partes[1]);
                    if (pAct != null) pAct.setActivo(true);
                    break;
                case "CLON_LLAMAR":
                    Cliente c = colaClientesEnEspera.poll();
                    Puesto pLlam = buscarPuestoPorId(partes[1]);
                    if (pLlam != null && c != null) pLlam.asignarClienteAlPuesto(c);
                    break;
                case "CLON_RELLAMAR":
                    Puesto pRel = buscarPuestoPorId(partes[1]);
                    if (pRel != null) pRel.disminuirReintento();
                    break;
                case "CLON_DESCONECTAR":
                    Puesto pDesc = buscarPuestoPorId(partes[1]);
                    if (pDesc != null) pDesc.setActivo(false);
                    break;
            }
            return "OK_CLON";
        }

        if (esRespaldo) return Protocolo.ERR_CONEXION;

        // Lógica de Servidor Activo (Solo Principal)
        switch (comando) {
            case Protocolo.CMD_LLAMAR: 
            	return llamarSiguienteCliente(partes[1]);
            case Protocolo.CMD_REGISTRO:
                Puesto p = buscarPuestoPorId(partes[2]);
                if (p != null) 
                	
                	return Protocolo.ERR_PUESTO_EXISTE;
                anadirPuesto(partes[1], partes[3], partes[2]);
                
                return Protocolo.OK_REGISTRADO;
            case Protocolo.CMD_RELLAMAR:
            	return Rellamar(partes[1]);
            case Protocolo.CMD_INFO_FILA: 
            	return String.valueOf(colaClientesEnEspera.size());
            case Protocolo.CMD_NUEVO_CLIENTE: 
            	return validaCliente(partes[1]);
            case Protocolo.CMD_DESCONECTAR:
                Puesto pd = buscarPuestoPorId(partes[1]);
                if (pd != null) pd.setActivo(false);
                replicarEnRespaldo("CLON_DESCONECTAR" + Protocolo.SEPARADOR + partes[1]);
                return Protocolo.OK_DESCONECTAR;
            default: return Protocolo.ERR_COMANDO;
        }
    }

    // --- Métodos de apoyo ---
    public synchronized void anadirCliente(String dni) {
        colaClientesEnEspera.addLast(new Cliente(dni));
        replicarEnRespaldo("CLON_CLIENTE" + Protocolo.SEPARADOR + dni);
    }

    public void anadirPuesto(String ip, String puertoLocal, String idPuesto) {
        listaPuestosRegistrados.add(new Puesto(ip, puertoLocal, idPuesto, true));
        replicarEnRespaldo("CLON_PUESTO" + Protocolo.SEPARADOR + ip + Protocolo.SEPARADOR + puertoLocal + Protocolo.SEPARADOR + idPuesto);
    }

    private Puesto buscarPuestoPorId(String id) {
        for (Puesto p : listaPuestosRegistrados) {
            if (String.valueOf(p.getNroPuesto()).equals(id) || p.getPuerto().equals(id)) return p;
        }
        return null;
    }

    public synchronized String validaCliente(String dni) {
        for (Cliente c : colaClientesEnEspera) {
            if (c.getDni().equals(dni)) return Protocolo.ERR_DNI_DUPLICADO;
        }
        anadirCliente(dni);
        return Protocolo.OK_CLIENTE_CREADO;
    }

    private String llamarSiguienteCliente(String nroPuesto) {
        Puesto p = buscarPuestoPorId(nroPuesto);
        if (p == null) return Protocolo.ERR_PUESTO_NO_EXISTE;
        Cliente c = colaClientesEnEspera.poll();
        if (c != null) {
            p.asignarClienteAlPuesto(c);
            actualizarPantallasGigantes(c.getDni(), nroPuesto);
            replicarEnRespaldo("CLON_LLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            return c.getDni();
        }
        return Protocolo.ERR_FILA_VACIA;
    }

    private String Rellamar(String nroPuesto) {
        Puesto p = buscarPuestoPorId(nroPuesto);
        if (p != null && p.getReintentos() > 0) {
            p.disminuirReintento();
            actualizarPantallasGigantes(p.getDni(), nroPuesto);
            replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            return p.getDni();
        }
        return Protocolo.SIN_REINTENTOS;
    }

    private void actualizarPantallasGigantes(String dni, String idPuesto) {
        String mensaje = Protocolo.MSG_ACTUALIZAR_MONITOR + Protocolo.SEPARADOR + dni + Protocolo.SEPARADOR + "Puesto " + idPuesto;
        Iterator<PrintWriter> it = monitoresConectados.iterator();
        while (it.hasNext()) {
            PrintWriter out = it.next();
            try { out.println(mensaje); } catch (Exception e) { it.remove(); }
        }
    }
}