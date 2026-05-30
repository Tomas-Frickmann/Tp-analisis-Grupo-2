package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import interfaces.IMonitorListener;
import util.Protocolo; 
import util.GestorJson; 
import seguridad.SeguridadFacade;
public class MonitorModelo {
    
    private LinkedList<String> historialAtendidos = new LinkedList<>();
    private IMonitorListener listener;
    private final int MAX_HISTORIAL = 5;
    
    
    private String ipLiderActual = null;
    private int puertoLiderActual = -1;
    
    public MonitorModelo() {
        historialAtendidos.add("Esperando turnos...");
    }

    public void setListener(IMonitorListener listener) {
        this.listener = listener;
    }

    public void iniciarEscuchaPermanente() {
        new Thread(() -> {
            
            while (true) { 
                
                
                if (ipLiderActual == null) {
                    String[] principal = GestorJson.obtenerPrincipalActivo();
                    if (principal != null) {
                        ipLiderActual = principal[0];
                        puertoLiderActual = Integer.parseInt(principal[1]);
                    }
                }

                if (ipLiderActual != null) {
                    try {
                        System.out.println("Monitor: Conectando al líder en " + ipLiderActual + ":" + puertoLiderActual);
                        
                        conectarYEscuchar(ipLiderActual, puertoLiderActual);
                    } 
                    catch (Exception e) {
                        System.out.println("Monitor: Conexión perdida con el servidor. Buscando en el JSON...");
                        mostrarAlertaReconexion();
                        
                        ipLiderActual = null;
                    }
                } else {
                    System.out.println("Monitor: No hay líder activo en la red. Esperando ascenso...");
                    mostrarAlertaReconexion();
                }

                
                try {
                    Thread.sleep(3000); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break; 
                }
            }
        }).start();
    }
    
    private void mostrarAlertaReconexion() {
        if (listener != null) {
            LinkedList<String> alertaPantalla = new LinkedList<>();
            alertaPantalla.add(" RECONECTANDO...");
            alertaPantalla.addAll(historialAtendidos); 
            listener.alRecibirNuevoLlamado(alertaPantalla);
        }
    }
    
    private void conectarYEscuchar(String ip, int puerto) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            
            out.println(Protocolo.CMD_REGISTRO_MONITOR);
            System.out.println("Monitor: Conectado exitosamente.");

            
            if (listener != null) {
                listener.alRecibirNuevoLlamado(new LinkedList<>(historialAtendidos));
            }

            String mensajeDelServidor;
            
            while ((mensajeDelServidor = in.readLine()) != null) {
                if (mensajeDelServidor.startsWith(Protocolo.MSG_ACTUALIZAR_MONITOR)) {
                    String[] partes = mensajeDelServidor.split(Protocolo.SEPARADOR);
                    String dni = SeguridadFacade.descifrarDni( partes[1]);
                    String puesto = partes[2];
                    procesarEntrada(dni, puesto);
                }
            }
            
            throw new Exception("El servidor cerró el flujo de datos de manera abrupta.");
        }
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