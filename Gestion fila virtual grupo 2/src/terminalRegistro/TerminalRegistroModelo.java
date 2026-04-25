package terminalRegistro;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import util.Protocolo;

public class TerminalRegistroModelo {
    private String ipServidor;
    private int puertoServidor;

    public TerminalRegistroModelo(String ipDestino, int puertoDestino) {
        this.ipServidor = "localhost"; 
        this.puertoServidor = puertoDestino;
    }

    public String enviarTurnoPorSocket(String dni) {
        
        String mensaje = Protocolo.CMD_NUEVO_CLIENTE + Protocolo.SEPARADOR + dni;

        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            out.println(mensaje);
            System.out.println("Cliente: Enviado mensaje al servidor: " + mensaje);
            return in.readLine(); 
            
        } catch (Exception e) {
            return Protocolo.ERR_CONEXION;
        }
    }

    
}