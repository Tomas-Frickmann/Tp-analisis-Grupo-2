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
	private int puertoEscucha;
    
    public void setListener(IMonitorListener listener) {
        this.listener = listener;
    }public void iniciarServidor() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(puertoEscucha)) {
                
                
                while (true) {
                    
                    try (Socket s = ss.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        
                        String dni;
                        
                        while ((dni = in.readLine()) != null) {
                            procesarEntrada(dni);
                        }
                    } catch (Exception e) {
                        
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
public void setPuertoEscucha(int puertoEscucha) {
	// TODO Auto-generated method stub
	this.puertoEscucha = puertoEscucha;
	
}




}
