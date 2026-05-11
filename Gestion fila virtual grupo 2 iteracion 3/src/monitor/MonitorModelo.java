package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import interfaces.IMonitorListener;
import util.ConfigServidor;
import util.Protocolo; 
import util.GestorJson; // Importamos el gestor

public class MonitorModelo {
    
    private LinkedList<String> historialAtendidos = new LinkedList<>();
    private IMonitorListener listener;
    private final int MAX_HISTORIAL = 5;
    
    public MonitorModelo() {
        historialAtendidos.add("Esperando turnos...");
        // Ya no necesitamos usar ConfigServidor para sacar la IP
    }

    public void setListener(IMonitorListener listener) {
        this.listener = listener;
    }

    public void iniciarEscuchaPermanente() {
        new Thread(() -> {
            // El monitor nunca muere. Si no hay conexión, sigue intentando para siempre.
            while (true) { 
                String[] principal = GestorJson.obtenerPrincipalActivo();

                if (principal != null) {
                    String ipActual = principal[0];
                    int puertoActual = Integer.parseInt(principal[1]);
                    
                    try {
                        System.out.println("Monitor: Conectando al líder en " + ipActual + ":" + puertoActual);
                        conectarYEscuchar(ipActual, puertoActual);
                    } 
                    catch (Exception e) {
                        System.out.println("Monitor: Conexión perdida con el servidor.");
                        mostrarAlertaReconexion();
                    }
                } else {
                    System.out.println("Monitor: No hay líder activo en la red. Esperando ascenso...");
                    mostrarAlertaReconexion();
                }

                // Pausa antes de volver a buscar en el JSON
                try {
                    Thread.sleep(3000); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break; // Solo sale si se cierra la aplicación
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

            // Nos registramos
            out.println(Protocolo.CMD_REGISTRO_MONITOR);
            System.out.println("Monitor: Conectado exitosamente.");

            // Limpiamos el texto de "Reconectando..." si volvimos a conectar
            if (listener != null) {
                listener.alRecibirNuevoLlamado(new LinkedList<>(historialAtendidos));
            }

            String mensajeDelServidor;
            // Escucha bloqueante: se queda acá esperando que el servidor llame clientes
            while ((mensajeDelServidor = in.readLine()) != null) {
                if (mensajeDelServidor.startsWith(Protocolo.MSG_ACTUALIZAR_MONITOR)) {
                    String[] partes = mensajeDelServidor.split(Protocolo.SEPARADOR);
                    String dni = partes[1];
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