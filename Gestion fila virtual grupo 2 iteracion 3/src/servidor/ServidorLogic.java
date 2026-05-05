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

public class ServidorLogic {
    
    private ConfigServidor config;
    private boolean esRespaldo;
    private String nombreServidor;
    
    private int puertoServidor; 
    private String ip; 
    
    private LinkedList<Puesto> listaPuestosRegistrados = new LinkedList<>();
    private LinkedList<Cliente> colaClientesEnEspera = new LinkedList<>();
    private LinkedList<PrintWriter> monitoresConectados = new LinkedList<>();

    public ServidorLogic(ConfigServidor config, boolean esRespaldo) {
        this.config = config;
        this.esRespaldo = esRespaldo;
        
        if (!esRespaldo) {
            this.nombreServidor = "Servidor PRINCIPAL";
            this.puertoServidor = config.getPuertoPrincipal();
            this.ip = config.getIpPrincipal();
        } else {
            this.nombreServidor = "Servidor de RESPALDO (Standby)";
            this.puertoServidor = config.getPuertoRespaldo();
            this.ip = config.getIpRespaldo();
        }
    }

    //Lo pongo en un hilo aparte para que no me moleste al principal
    private void replicarEnRespaldo(String comandoOculto) {
        
        if (!esRespaldo && this.nombreServidor.equals("Servidor PRINCIPAL")) {
            new Thread(() -> {
                try (Socket s = new Socket(config.getIpRespaldo(), config.getPuertoRespaldo());
                     PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                    out.println(comandoOculto);
                } catch (Exception e) {
                    
                }
            }).start();
        }
    }

    public synchronized void anadirCliente(String dni) {
        colaClientesEnEspera.addLast(new Cliente(dni));
        System.out.println(nombreServidor + ": Cliente " + dni + " añadido a la fila normal.");
        
        
        replicarEnRespaldo("CLON_CLIENTE" + Protocolo.SEPARADOR + dni);
    }

    public void anadirPuesto(String ip, String puertoLocal, String idPuesto) {
        Puesto nuevo = new Puesto(ip, puertoLocal, idPuesto, true);
        listaPuestosRegistrados.add(nuevo);
        System.out.println(nombreServidor + ": Puesto " + idPuesto + " registrado con éxito.");
        
        replicarEnRespaldo("CLON_PUESTO" + Protocolo.SEPARADOR + ip + Protocolo.SEPARADOR + puertoLocal + Protocolo.SEPARADOR + idPuesto);
    }

    public Puesto buscarPuestoPorId(String idBuscado) {
        int idNumerico = Integer.parseInt(idBuscado); 
        for (Puesto aux : listaPuestosRegistrados) {
            if (aux.getPuerto().equals(idBuscado) || aux.getNroPuesto() == idNumerico) {
                return aux;
            }
        }
        return null;
    }

    public String validaPuesto(String ip, String puertoLocal, String idPuesto) {
        int nroPuestoBuscado = Integer.parseInt(idPuesto); 
        for (Puesto aux : listaPuestosRegistrados) {
            if (aux.getIp().equals(ip) && aux.getNroPuesto() == nroPuestoBuscado) {
                if (aux.isActivo()) {
                    return "ESTA_ACTIVO";
                } else {
                    aux.setActivo(true);
                    
                    replicarEnRespaldo("CLON_ACTIVA_PUESTO" + Protocolo.SEPARADOR + idPuesto);
                    return "PUESTO_DISPONIBLE"; 
                }
            }   
        }
        anadirPuesto(ip, puertoLocal, idPuesto);
        return "PUESTO_DISPONIBLE"; 
    }

    public synchronized String validaCliente(String dni) {
        for (Cliente aux : colaClientesEnEspera) {
            if (aux.getDni().equals(dni)) return Protocolo.ERR_DNI_DUPLICADO; 
        }
        anadirCliente(dni);
        return Protocolo.OK_CLIENTE_CREADO;
    }

    public void actualizarPantallasGigantes(String dni, String idPuesto) {
        String mensaje = Protocolo.MSG_ACTUALIZAR_MONITOR + Protocolo.SEPARADOR + dni + Protocolo.SEPARADOR + "Puesto " + idPuesto;
        Iterator<PrintWriter> it = monitoresConectados.iterator();
        while (it.hasNext()) {
            PrintWriter outMonitor = it.next();
            try {
                outMonitor.println(mensaje);
            } catch (Exception e) {
                it.remove(); 
            }
        }
    }

    public synchronized String llamarSiguienteCliente(String nroPuesto) {
        try { Thread.sleep(500); } catch (Exception e) {}
        Puesto p = buscarPuestoPorId(nroPuesto); 
        if (p == null) 
            return Protocolo.ERR_PUESTO_NO_EXISTE; 
        
        Cliente sig = colaClientesEnEspera.poll();
        if(sig != null) {
            p.asignarClienteAlPuesto(sig);
            
            
            if (!esRespaldo) {
                actualizarPantallasGigantes(sig.getDni(), nroPuesto);
                
                replicarEnRespaldo("CLON_LLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            }
            
            return sig.getDni(); 
        } else {
            return Protocolo.ERR_FILA_VACIA; 
        }
    }
    
    public synchronized String Rellamar(String nroPuesto) {
        try { Thread.sleep(500); } catch (Exception e) {}
        Puesto p = buscarPuestoPorId(nroPuesto); 
        if (p == null) return Protocolo.ERR_PUESTO_NO_EXISTE;        
        if (p.getReintentos()>0) {
            p.disminuirReintento();
            
            if (!esRespaldo) {
                actualizarPantallasGigantes(p.getDni(), nroPuesto);
                replicarEnRespaldo("CLON_RELLAMAR" + Protocolo.SEPARADOR + nroPuesto);
            }
            
            return p.getDni(); 
        }
        else {
            return Protocolo.SIN_REINTENTOS;
        }
    }

    private String desconectar_puesto(String puesto) {
        Puesto p = buscarPuestoPorId(puesto);
        if (p != null) {
            p.setActivo(false);
            if (!esRespaldo) {
                replicarEnRespaldo("CLON_DESCONECTAR" + Protocolo.SEPARADOR + puesto);
            }
        }
        return Protocolo.OK_DESCONECTAR;
    }

    public void iniciarServidor() {
        //ahora abro los dos puertos para poder hacer todo lo otro por detras
        abrirPuertoParaClientes();
        
        if (esRespaldo) {
            System.out.println(">>> INICIANDO " + nombreServidor + " MODO VIGILANCIA <<<");
            iniciarPing_Echo();
        } else {
            System.out.println(">>> INICIANDO " + nombreServidor + " MODO ACTIVO <<<");
        }
    }

    private void iniciarPing_Echo() {
        new Thread(() -> {
            int fallos = 0;
            while (esRespaldo) { 
                try {
                    Thread.sleep(config.getintervaloPing()); 
                    
                    try (Socket s = new Socket(config.getIpPrincipal(), config.getPuertoPrincipal())) {
                        fallos = 0; 
                    } catch (Exception e) {
                        fallos++;
                        System.out.println(nombreServidor + ": Ping/Echo falló (" + fallos + "/" + config.getMaxIntentosFallidos() + ")");
                        
                        if (fallos >= config.getMaxIntentosFallidos()) {
                            System.out.println(">>> ¡ALERTA! Servidor Principal caído. Asumiendo el control... <<<");
                            this.esRespaldo = false;
                            this.nombreServidor = "Servidor PRINCIPAL (Recuperado)";
                            
                        }
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }).start();
    }

    private void abrirPuertoParaClientes() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puertoServidor)) {
                System.out.println(">>> " + nombreServidor + " ESCUCHANDO CONEXIONES EN PUERTO " + puertoServidor + " <<<");
                while (true) {
                    Socket s = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    String mensaje = in.readLine(); 
                    
                    if (mensaje != null) {
                        String[] partes = mensaje.split(Protocolo.SEPARADOR); 
                        
                        if (partes[0].equals(Protocolo.CMD_REGISTRO_MONITOR)) {
                            monitoresConectados.add(out);
                            System.out.println(nombreServidor + ": Monitor conectado.");
                        }                  
                        else {
                            String respuesta = procesarAccion(partes);
                            out.println(respuesta); 
                            s.close(); 
                        }
                    }
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }).start();
    }

    private String procesarAccion(String[] partes) {
        String comando = partes[0];
        
        
        if (esRespaldo && comando.startsWith("CLON_")) {
            if (comando.equals("CLON_CLIENTE")) {
                colaClientesEnEspera.addLast(new Cliente(partes[1]));
                System.out.println("(CLON) Cliente " + partes[1] + " anotado en silencio.");
            } 
            else if (comando.equals("CLON_PUESTO")) {
                Puesto nuevo = new Puesto(partes[1], partes[2], partes[3], true);
                listaPuestosRegistrados.add(nuevo);
                System.out.println("(CLON) Puesto " + partes[3] + " registrado en silencio.");
            }
            else if (comando.equals("CLON_ACTIVA_PUESTO")) {
                Puesto p = buscarPuestoPorId(partes[1]);
                if (p != null) p.setActivo(true);
            }
            else if (comando.equals("CLON_LLAMAR")) {
                Cliente c = colaClientesEnEspera.poll();
                Puesto p = buscarPuestoPorId(partes[1]);
                if (p != null && c != null) p.asignarClienteAlPuesto(c);
                System.out.println("(CLON) Cliente " + (c != null ? c.getDni() : "") + " sacado de la fila en silencio.");
            }
            else if (comando.equals("CLON_RELLAMAR")) {
                Puesto p = buscarPuestoPorId(partes[1]);
                if (p != null) p.disminuirReintento();
            }
            else if (comando.equals("CLON_DESCONECTAR")) {
                Puesto p = buscarPuestoPorId(partes[1]);
                if (p != null) p.setActivo(false);
            }
           
            return "OK_CLON";
        }

        
        if (esRespaldo && !comando.startsWith("CLON_")) {
            return Protocolo.ERR_CONEXION;
        }

        
        if (comando.equals(Protocolo.CMD_LLAMAR)) {
            if (!colaClientesEnEspera.isEmpty()) 
                return this.llamarSiguienteCliente(partes[1]); 
            return Protocolo.ERR_FILA_VACIA;
        }
        else if (comando.equals(Protocolo.CMD_REGISTRO)) {
            String res= this.validaPuesto(partes[1], partes[3], partes[2]); 
            if(res.equalsIgnoreCase("PUESTO_DISPONIBLE"))
                return Protocolo.OK_REGISTRADO;
            else if(res.equalsIgnoreCase("ESTA_ACTIVO"))
                return Protocolo.ERR_PUESTO_EXISTE;
        }
        else if (comando.equals(Protocolo.CMD_RELLAMAR)) {
            return this.Rellamar(partes[1]);
        }
        else if (comando.equals(Protocolo.CMD_INFO_FILA)) {
            return String.valueOf(colaClientesEnEspera.size());
        }
        else if (comando.equals(Protocolo.CMD_NUEVO_CLIENTE)) {
            return this.validaCliente(partes[1]);
        }
        else if(comando.equalsIgnoreCase(Protocolo.CMD_DESCONECTAR)) {
            return this.desconectar_puesto(partes[1]);
        }
        
        return Protocolo.ERR_COMANDO;
    }  
}