package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import interfaces.IMonitorListener;
import util.Protocolo; // <-- IMPORTAMOS LAS CONSTANTES

public class MonitorModelo {
	
	private LinkedList<String> historialAtendidos = new LinkedList<>();
    private IMonitorListener listener;
    private final int MAX_HISTORIAL = 5;
	private String ipServidor = "localhost";
    private int puertoServidor = 5000;
    
    public void setListener(IMonitorListener listener) {
        this.listener = listener;
    }

    public void setConexion(String ip, int puerto) {
        this.ipServidor = ip;
        this.puertoServidor = puerto;
    }


    public void iniciarEscuchaPermanente() {
        new Thread(() -> {
            try {

                Socket s = new Socket(ipServidor, puertoServidor);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                
             
                out.println(Protocolo.CMD_REGISTRO_MONITOR);           
                String mensajeDelServidor;
                
                while ((mensajeDelServidor = in.readLine()) != null) {
                    
                	if (mensajeDelServidor.startsWith(Protocolo.MSG_ACTUALIZAR_MONITOR)) {
                        String[] partes = mensajeDelServidor.split(Protocolo.SEPARADOR);
                        String dni = partes[1];
                        String puesto = partes[2];
                        procesarEntrada(dni, puesto);
                    }
                }
            } catch (Exception e) {

                if (listener != null) {
                    listener.alOcurrirError("Se perdió la conexión con el Servidor Central.", "Error de Red");
                }
            }
        }).start();
    }

    private void procesarEntrada(String dni, String puesto) {

        String turnoFormateado = dni + "  -  " + puesto;
        historialAtendidos.removeIf(elementoViejo -> elementoViejo.contains(dni));
        historialAtendidos.addFirst(turnoFormateado);
        if (historialAtendidos.size() > MAX_HISTORIAL) {
            historialAtendidos.removeLast();
        }
        if (listener != null) {
            listener.alRecibirNuevoLlamado(new LinkedList<>(historialAtendidos));
        }
    }
    }
