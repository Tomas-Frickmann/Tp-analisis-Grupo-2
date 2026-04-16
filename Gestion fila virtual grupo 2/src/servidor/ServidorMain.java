package servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import cliente.Cliente;

public class ServidorMain {
    private int puertoServidor = 5000; // El puerto donde todos se conectan
    private LinkedList<Puesto> listaPuestosRegistrados = new LinkedList<>();
    private Queue<Cliente> colaClientesEnEspera = new LinkedList<>();

    public void anadirCliente(String dni) {
        colaClientesEnEspera.add(new Cliente(dni));
        System.out.println("Servidor: Cliente " + dni + " añadido a la fila.");
    }

    public void anadirPuesto(String ip, String puerto, String puesto) {
        Puesto nuevo = new Puesto(ip, puerto, puesto);
        listaPuestosRegistrados.add(nuevo);
        System.out.println("Servidor: Puesto " + puesto + " registrado con éxito.");
    }

    public Puesto buscarPuestoPorId(String idBuscado) {
		// 1. Convertimos el ID que llega por red (String) a un número entero
		int idNumerico = Integer.parseInt(idBuscado); 
		
		Iterator<Puesto> it = listaPuestosRegistrados.iterator();
		while (it.hasNext()) {
			Puesto aux = it.next();
			
			// 2. Comparamos el Puerto con .equals() (porque es String)
			// Y comparamos el NroPuesto con == (porque es int)
			if (aux.getPuerto().equals(idBuscado) || aux.getNroPuesto() == idNumerico) {
				return aux;
			}
		}
		return null;
	}

    public boolean validaPuesto(String ip, String puerto, String puesto) {
		Iterator<Puesto> it = listaPuestosRegistrados.iterator();
		boolean existe = false;
		
		int nroPuestoBuscado = Integer.parseInt(puesto); // Lo pasamos a int
		
		while (it.hasNext()){
			Puesto aux = it.next();
			// Comparamos usando == para el int
			if (aux.getIp().equals(ip) && aux.getNroPuesto() == nroPuestoBuscado){
				existe = true;
				break;
			}	
		}
		if (!existe){
			anadirPuesto(ip, puerto, puesto);
			return true; 
		}
		return false; 
	}

    public String llamarSiguienteCliente(String nroPuesto) {
        Puesto p = buscarPuestoPorId(nroPuesto); 
        
        if (p == null) return "ERROR:PUESTO_NO_EXISTE";
        
        Cliente sig = colaClientesEnEspera.poll();
        if(sig != null) {
            p.asignarClienteAlPuesto(sig);
            return sig.getDni(); 
        } else {
            return "ERROR:FILA_VACIA";
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
                        String[] partes = mensaje.split(";");
                        String respuesta = procesarAccion(partes);
                        out.println(respuesta); 
                    }
                    s.close();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private String procesarAccion(String[] partes) {
        String comando = partes[0];
        
        if (comando.equals("LLAMAR")) {
            if (!colaClientesEnEspera.isEmpty()) {
                return this.llamarSiguienteCliente(partes[1]); 
            }
            return "ERROR:FILA_VACIA";
        }
        else if (comando.equals("REGISTRO")) {
            String ip = partes[1];
            String idPuesto = partes[2]; 
            boolean exito = this.validaPuesto(ip, idPuesto, idPuesto);
            
            if (exito) return "OK:REGISTRADO";
            else return "ERROR:PUESTO_YA_EXISTE";
        }
        else if (comando.equals("RE_LLAMAR")) {
            return "OK:RE_LLAMADO";
        }else if (comando.equals("INFO_FILA")) {
	        
	        return String.valueOf(colaClientesEnEspera.size());
	    }
        
        return "ERROR:COMANDO_INVALIDO";
    }  

    public static void main(String[] args) {
        ServidorMain servidor = new ServidorMain();
       
        servidor.anadirCliente("35123456"); 
        servidor.anadirCliente("1234444"); 
        servidor.iniciarServidor();
    }
}