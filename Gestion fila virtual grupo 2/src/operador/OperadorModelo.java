package operador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class OperadorModelo {
    
    // Datos del Servidor Central
    private String ipServidor = "localhost";
    private int puertoServidor = 5000; // Debe coincidir con el puerto de ServidorMain

    // Datos de este Puesto (Operador)
    private String miIp = "127.0.0.1";
    private String idPuesto; // Ej: "1", "2"
    private int MiPuertoLocal; // Puerto local para recibir notificaciones (si se implementa)

    public OperadorModelo() {}

    // Configuramos la IP real y el ID del puesto al iniciar
    public void configurarPuesto(String nroPuesto) {
        this.idPuesto = nroPuesto;
        try {
            this.miIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.miIp = "127.0.0.1";
        }
    }

    // 1. Handshake inicial con el Servidor
    public boolean registrarEnServidor() {
    	this.MiPuertoLocal= 6000 + Integer.parseInt(this.idPuesto); // esto deberia ser distinto pero como es todo en una maquina lo dejo asi
        try (Socket s = new Socket(ipServidor, puertoServidor); 
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            // Enviamos: REGISTRO;192.168.1.5;1
            out.println("REGISTRO;" + this.miIp + ";" + this.idPuesto+";"+this.MiPuertoLocal);
            
            String respuesta = in.readLine();
            
            if (respuesta != null && respuesta.equals("OK:REGISTRADO")) {
                System.out.println("Puesto " + this.idPuesto + " registrado exitosamente.");
                return true;
            } else {
                System.out.println("El servidor rechazó el registro: " + respuesta);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error al intentar conectarse al Servidor Central.");
            return false;
        }
    }

    // 2. Pedir el siguiente cliente a la Fila (que ahora está en el servidor)
    public String llamarSiguiente() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            // Enviamos: LLAMAR;1
            out.println("LLAMAR;" + this.idPuesto);
            
            // Retorna el DNI o "ERROR:FILA_VACIA"
            return in.readLine(); 
            
        } catch (Exception e) {
            return "ERROR:CONEXION";
        }
    }

    // 3. Nuevo requerimiento: Re-notificar al cliente (Máximo 3 veces)
    public String reLlamar() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            // Enviamos: RE_LLAMAR;1
            out.println("RE_LLAMAR;" + this.idPuesto);
            
            // Retorna "OK:RE_LLAMADO" o "ERROR:MAX_INTENTOS"
            return in.readLine(); 
            
        } catch (Exception e) {
            return "ERROR:CONEXION";
        }
    }
    public String obtenerTamañoFila() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            out.println("INFO_FILA");
            return in.readLine(); // Devuelve "0", "1", "5", etc.
            
        } catch (Exception e) {
            return "-"; // Si hay error de red, devolvemos un guión
        }
    }
}