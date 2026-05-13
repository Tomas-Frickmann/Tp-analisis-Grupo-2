package terminalRegistro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import util.ConfigServidor;
import util.Protocolo;
import util.GestorJson;

public class TerminalRegistroModelo {
    
    private ConfigServidor config;
    
    // VARIABLES DE CACHÉ: Evitan leer el disco rígido innecesariamente
    private String ipLiderActual = null;
    private int puertoLiderActual = -1;

    public TerminalRegistroModelo() {
        this.config = new ConfigServidor("config_servidores.properties");
    }

    public String enviarTurnoPorSocket(String dni) {
        String mensaje = Protocolo.CMD_NUEVO_CLIENTE + Protocolo.SEPARADOR + dni;
        int intentosRestantes = config.getMaxIntentosFallidos();

        while (intentosRestantes > 0) {
            // 1. CACHÉ: Solo buscamos en el JSON si no sabemos quién manda
            if (ipLiderActual == null) {
                String[] principal = GestorJson.obtenerPrincipalActivo();
                if (principal != null) {
                    ipLiderActual = principal[0];
                    puertoLiderActual = Integer.parseInt(principal[1]);
                }
            }

            if (ipLiderActual != null) {
                try {
                    // 2. Intentamos conectar usando la info en memoria
                    return conectarYEnviar(ipLiderActual, puertoLiderActual, mensaje);
                } 
                catch (Exception e) {
                    System.err.println("[RED] El líder (" + puertoLiderActual + ") no responde. Buscando en el JSON...");
                    // 3. Si falla la red, vaciamos el caché para obligar a leer el JSON en el próximo intento
                    ipLiderActual = null; 
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