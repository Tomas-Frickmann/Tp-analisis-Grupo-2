package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import interfaces.IMonitorListener;

public class MonitorModelo {
	
	
	private LinkedList<String> historialAtendidos = new LinkedList<>();
    private IMonitorListener listener;
    private final int MAX_HISTORIAL = 5;
	
    
    public void setListener(IMonitorListener listener) {
        this.listener = listener;
    }
public void iniciarServidor() {
    new Thread(() -> {
        try (ServerSocket ss = new ServerSocket(6000)) {
            while (true) {
                Socket s = ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String dni = in.readLine();
                
              
                   this.procesarEntrada(dni);
                    
                    
                  
                    
                
            }
        } catch (Exception e) {
            if (listener != null) listener.alOcurrirError("Error de red","Error");
        }
    }).start();
}
private void procesarEntrada(String dni) {
	historialAtendidos.addFirst(dni);
	if (historialAtendidos.size() > MAX_HISTORIAL) {
        historialAtendidos.removeLast();
    }

    
    if (listener != null) {
        listener.alRecibirNuevoLlamado(new LinkedList<>(historialAtendidos));
    }
    
}




}
