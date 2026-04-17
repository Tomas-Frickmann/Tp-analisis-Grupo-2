package servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import util.Protocolo;

public class ServidorMain {
    private int puertoServidor = 5000; 
    private LinkedList<Puesto> listaPuestosRegistrados = new LinkedList<>();
    private LinkedList<Cliente> colaClientesEnEspera = new LinkedList<>();
    private LinkedList<PrintWriter> monitoresConectados = new LinkedList<>();

    public synchronized void anadirCliente(String dni) {
        colaClientesEnEspera.addLast(new Cliente(dni));
        System.out.println("Servidor: Cliente " + dni + " añadido a la fila normal.");
    }

    public void anadirPuesto(String ip, String puertoLocal, String idPuesto) {
        Puesto nuevo = new Puesto(ip, puertoLocal, idPuesto);
        listaPuestosRegistrados.add(nuevo);
        System.out.println("Servidor: Puesto " + idPuesto + " registrado con éxito.");
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

    public boolean validaPuesto(String ip, String puertoLocal, String idPuesto) {
        int nroPuestoBuscado = Integer.parseInt(idPuesto); 
        for (Puesto aux : listaPuestosRegistrados) {
            if (aux.getIp().equals(ip) && aux.getNroPuesto() == nroPuestoBuscado) {
                return false; 
            }   
        }
        anadirPuesto(ip, puertoLocal, idPuesto);
        return true; 
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
            actualizarPantallasGigantes(sig.getDni(), nroPuesto);
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
        	actualizarPantallasGigantes(p.getDni(), nroPuesto);
        	return p.getDni(); 
        	}
        else {
        	return Protocolo.SIN_REINTENTOS;
        }
        
          
        
    }
    public void iniciarServidor() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puertoServidor)) {
            	System.out.println(">>> SERVIDOR CENTRAL INICIADO EN PUERTO " + puertoServidor + " <<<");
                while (true) {
                    Socket s = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    String mensaje = in.readLine(); 
                    if (mensaje != null) {
                        String[] partes = mensaje.split(Protocolo.SEPARADOR); 
                        
                        if (partes[0].equals(Protocolo.CMD_REGISTRO_MONITOR)) {
                            monitoresConectados.add(out);
                            System.out.println("Servidor: Monitor de Sala de Espera conectado.");
                        }                  
                        else {
                            String respuesta = procesarAccion(partes);
                            out.println(respuesta); 
                            s.close(); 
                        }
                    }
                }
            } catch (Exception e)
            { e.printStackTrace(); }
        }).start();
    }

    private String procesarAccion(String[] partes) {
        String comando = partes[0];
        
        if (comando.equals(Protocolo.CMD_LLAMAR)) {
            if (!colaClientesEnEspera.isEmpty()) 
            	return this.llamarSiguienteCliente(partes[1]); 
            return Protocolo.ERR_FILA_VACIA;
        }
        else if (comando.equals(Protocolo.CMD_REGISTRO)) {
            boolean exito = this.validaPuesto(partes[1], partes[3], partes[2]); // ip, puertoLocal, idPuesto
            return exito ? Protocolo.OK_REGISTRADO : Protocolo.ERR_PUESTO_EXISTE;
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
        return Protocolo.ERR_COMANDO;
    }  

    public static void main(String[] args) {
        ServidorMain servidor = new ServidorMain();
        servidor.iniciarServidor();
    }
}