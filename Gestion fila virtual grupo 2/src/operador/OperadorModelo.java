package operador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import cliente.Cliente;
import interfaces.IFilaListener;

public class OperadorModelo {
    
    private String monitorIP = "localhost";
    private int puertoMonitor = 6000;
    private int puertoServidor = 5000;

    
    private ServerSocket ss;         
    private Socket sMonitor;         
    private PrintWriter outMonitor;  
    
    private Queue<Cliente> fila = new ConcurrentLinkedQueue<>();
    private IFilaListener observador;

    public OperadorModelo() {}

    public void conectarMonitor() {
        try {
            
            if (sMonitor == null || sMonitor.isClosed() || outMonitor == null) {
                this.sMonitor = new Socket();
                
                this.sMonitor.connect(new java.net.InetSocketAddress(monitorIP, puertoMonitor), 500);
                this.outMonitor = new PrintWriter(sMonitor.getOutputStream(), true);

            }
        } catch (IOException e) {
            
            desconectar(false);
        }
    }

    public String llamarSiguiente() {
        if (fila.isEmpty()) 
            return "VACIO";


        conectarMonitor(); 

        if (outMonitor != null) {
            Cliente c = fila.peek();
            outMonitor.println(c.getDni());
            if (outMonitor.checkError()) {
                desconectar(false);
                return "No_MONITOR";
            }
            fila.poll();
            if (observador != null) 
                observador.alCambiarFila(fila.size());               
            return "EXITO";
        }
        return "No_MONITOR";
    }

    
    public void desconectar(boolean server) {
        try {
            if (outMonitor != null) {
                outMonitor.close();
            }
            if (sMonitor != null) {
                sMonitor.close();
            }
            if (server && ss != null) {
				ss.close();
			}
            
        } catch (IOException e) {
            
        } finally {
            
            this.sMonitor = null;
            this.outMonitor = null;	
            
        }
    }

   
   

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                this.ss = new ServerSocket(puertoServidor); 
               

                while (!ss.isClosed()) {
                    Socket sCliente = ss.accept(); 
                    
                    new Thread(() -> {
                        
                        try (BufferedReader inCliente = new BufferedReader(new InputStreamReader(sCliente.getInputStream()))) {
                            String dni;
                            while ((dni = inCliente.readLine()) != null) {
                                fila.add(new Cliente(dni));
                                if (observador != null) 
                                	observador.alCambiarFila(fila.size());
                            }
                        } catch (IOException e) {
                            
                        }
                    }).start();
                }
            } catch (IOException e) {
                
            }
        }).start();
    }

   


    public int tamañoFila() { 
    	return fila.size(); 
    }
    public void setListener(IFilaListener listener) { 
    	this.observador = listener; 
    }

	public void setMonitorIP_purto(String ipMonitor, int puertoMonitor2) {
		// TODO Auto-generated method stub
		this.monitorIP = ipMonitor;
		this.puertoMonitor = puertoMonitor2;
		
	}

	public void setPuertoServidor(int puertoLocal) {
		// TODO Auto-generated method stub
		this.puertoServidor = puertoLocal;
		
	}
}