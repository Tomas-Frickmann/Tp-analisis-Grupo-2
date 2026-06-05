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
    
    private IMonitorListener listener;

    private String ipLiderActual = null;
    private int puertoLiderActual = -1;
    
    public MonitorModelo() {}

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
            listener.alRecibirNuevoLlamado(alertaPantalla);
        }
    }
    
    private void conectarYEscuchar(String ip, int puerto) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            out.println(Protocolo.CMD_REGISTRO_MONITOR);
            System.out.println("Monitor: Conectado exitosamente.");

            String mensajeDelServidor;
            
            while ((mensajeDelServidor = in.readLine()) != null) {
                if (mensajeDelServidor.startsWith(Protocolo.MSG_SYNC_MONITOR)) {
                    String[] partes = mensajeDelServidor.split(Protocolo.SEPARADOR);
                    LinkedList<String> listaParaPantalla = new LinkedList<>();
                    if (partes.length == 1) {
                        listaParaPantalla.add("Esperando turnos...");
                    } else {
                        for (int i = 1; i < partes.length; i += 2) {
                            String dniCifrado = partes[i];
                            String nroPuesto = partes[i+1];
                            
                            String dniDescifrado = SeguridadFacade.descifrarDni(dniCifrado);
                            listaParaPantalla.add(dniDescifrado + "  -   " + nroPuesto);
                        }
                    }
                    
                    if (listener != null) {
                        listener.alRecibirNuevoLlamado(listaParaPantalla);
                    }
                }
            }
            
            throw new Exception("El servidor cerró el flujo de datos de manera abrupta.");
        }
    }
}