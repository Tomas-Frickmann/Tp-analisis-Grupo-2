package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import interfaces.IMonitorListener;
import util.ConfigServidor;
import util.Protocolo; 

public class MonitorModelo {
    
    private LinkedList<String> historialAtendidos = new LinkedList<>();

    
    private IMonitorListener listener;
    private final int MAX_HISTORIAL = 5;
    
    private ConfigServidor config;

    public MonitorModelo() {
    	historialAtendidos.add("Esperando turnos...");
    	
        this.config = new ConfigServidor("config_servidores.properties");
    }

    public void setListener(IMonitorListener listener) {
        this.listener = listener;
        
    }

    public void iniciarEscuchaPermanente() {
        new Thread(() -> {
            int intentosRestantes = config.getMaxIntentosFallidos();

            while (intentosRestantes > 0) {
                try {
                    System.out.println("Monitor: Buscando Servidor Principal...");
                    conectarYEscuchar(config.getIpPrincipal(), config.getPuertoPrincipal());
                } 
                catch (Exception e1) {
                    System.out.println("Monitor: Conexión perdida con el Principal.");
                    
                    try {
                        System.out.println("Monitor: Buscando Servidor de Respaldo...");
                        conectarYEscuchar(config.getIpRespaldo(), config.getPuertoRespaldo());
                    } 
                    catch (Exception e2) {
                        intentosRestantes--;
                        System.out.println("⏳ Monitor: Ambos servidores caídos. Intentos: " + intentosRestantes);
                        
                        if (intentosRestantes > 0) {
                            if (listener != null) {
                               
                                LinkedList<String> alertaPantalla = new LinkedList<>();
                                alertaPantalla.add(" RECONECTANDO...");
                                
                                
                                alertaPantalla.addAll(historialAtendidos); 
                                
                                
                                listener.alRecibirNuevoLlamado(alertaPantalla);
                            }
                            
                            try {
                                Thread.sleep(2000); 
                            } catch (InterruptedException ie) {
                                ie.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            
            System.out.println("ERROR FATAL: Sistema offline.");
            if (listener != null) {
                LinkedList<String> alertaFatal = new LinkedList<>();
                alertaFatal.add("SISTEMA FUERA DE LÍNEA");
                alertaFatal.add("Aguarde, por favor...");
                listener.alRecibirNuevoLlamado(alertaFatal);
            }

        }).start();
    }
    
    private void conectarYEscuchar(String ip, int puerto) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

           
            out.println(Protocolo.CMD_REGISTRO_MONITOR);
            System.out.println("Monitor: Conectado exitosamente a " + ip + ":" + puerto);

            String mensajeDelServidor;
            
           
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