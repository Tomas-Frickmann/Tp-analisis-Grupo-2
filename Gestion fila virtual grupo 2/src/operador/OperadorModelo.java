package operador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import cliente.Cliente;
import interfaces.IFilaListener;

public class OperadorModelo {
	private String monitorIP = "localhost";
private Queue<Cliente> fila = new LinkedList<>();
	private IFilaListener observador;


public OperadorModelo() {
}
public String llamarSiguiente() {
	Cliente c=null;
    if (!fila.isEmpty()) {
    	 c =fila.peek();
    	 
    try (Socket s = new Socket(monitorIP, 6000);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
           out.println(c.getDni());
           fila.poll();
           return "EXITO";
       } catch (Exception ex) {
    	   return "No_MONITOR";
          
       }  
    }
    else
    	return "VACIO" ;
	}

public boolean notificarMonitor(String dni) {
	boolean coneccion=true;
    try (Socket s = new Socket(monitorIP, 6000);
         PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
        out.println(dni);
    } catch (Exception ex) {
    	coneccion=false;
       
    }
    return coneccion;
}
public Object tamañoFila() {
	// TODO Auto-generated method stub
	return fila.size();
}
public void iniciarServidor() {
    new Thread(() -> {
        try (ServerSocket ss = new ServerSocket(5000)) {
            while (true) {
                Socket s = ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String dni = in.readLine();
                
                if (dni != null) {
                    fila.add(new Cliente(dni));
                    
                    
                    if (observador != null) {
                        observador.alCambiarFila(fila.size());
                    }
                }
            }
        } catch (Exception e) {
            if (observador != null) observador.alOcurrirError("Error de red","Error");
        }
    }).start();
}
public void setListener(IFilaListener listener) {
	this.observador=listener;
}
}