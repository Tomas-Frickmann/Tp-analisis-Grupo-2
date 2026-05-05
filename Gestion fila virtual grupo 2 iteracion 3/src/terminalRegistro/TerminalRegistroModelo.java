package terminalRegistro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import util.ConfigServidor;
import util.Protocolo;

public class TerminalRegistroModelo {
    
    private ConfigServidor config;

    public TerminalRegistroModelo() {
        this.config = new ConfigServidor("config_servidores.properties");
    }

    public String enviarTurnoPorSocket(String dni) {
        String mensaje = Protocolo.CMD_NUEVO_CLIENTE + Protocolo.SEPARADOR + dni;
        
        
        int intentosRestantes = config.getMaxIntentosFallidos();

        while (intentosRestantes > 0) {
            
            try {
                return conectarYEnviar(config.getIpPrincipal(), config.getPuertoPrincipal(), mensaje);
            } 
            catch (Exception e1) {
                System.out.println("Principal no responde.");
                
                
                try {
                    return conectarYEnviar(config.getIpRespaldo(), config.getPuertoRespaldo(), mensaje);
                } 
                catch (Exception e2) {
                    
                    intentosRestantes--;
                    System.out.println("Respaldo tampoco responde. Intentos restantes: " + intentosRestantes);
                    
                    
                    if (intentosRestantes > 0) {
                        try {
                            Thread.sleep(1000); 
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
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