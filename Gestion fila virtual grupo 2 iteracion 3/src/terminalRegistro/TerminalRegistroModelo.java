package terminalRegistro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import util.ConfigServidor;
import util.Protocolo;
import util.GestorJson; // Importamos el gestor

public class TerminalRegistroModelo {
    
    private ConfigServidor config;

    public TerminalRegistroModelo() {
        this.config = new ConfigServidor("config_servidores.properties");
    }

    public String enviarTurnoPorSocket(String dni) {
        String mensaje = Protocolo.CMD_NUEVO_CLIENTE + Protocolo.SEPARADOR + dni;
        int intentosRestantes = config.getMaxIntentosFallidos();

        while (intentosRestantes > 0) {
            // Buscamos quién manda en este momento
            String[] principal = GestorJson.obtenerPrincipalActivo();

            if (principal != null) {
                String ipActual = principal[0];
                int puertoActual = Integer.parseInt(principal[1]);

                try {
                    return conectarYEnviar(ipActual, puertoActual, mensaje);
                } 
                catch (Exception e) {
                    System.err.println("[RED] El Principal del JSON (" + puertoActual + ") no responde.");
                    // Forzamos la baja en el JSON para ayudar a la red
                    GestorJson.marcarInactivo(ipActual, puertoActual);
                }
            } else {
                System.out.println("[SISTEMA] Buscando servidor líder en la red...");
            }

            intentosRestantes--;
            if (intentosRestantes > 0) {
                try {
                    Thread.sleep(2000); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println(" ERROR FATAL: Se agotaron los reintentos. Todo el sistema está offline.");
        return Protocolo.ERR_CONEXION;
    }

    private String conectarYEnviar(String ip, int puerto, String mensaje) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            System.out.println("Kiosco: Intentando enviar a " + ip + ":" + puerto + " -> " + mensaje);
            out.println(mensaje);
            return in.readLine(); 
        }
    }
}