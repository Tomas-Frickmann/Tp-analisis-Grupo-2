package operador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import util.Protocolo; // <-- IMPORTAMOS LAS CONSTANTES

public class OperadorModelo {
    
  
    private String ipServidor = "localhost";
    private int puertoServidor = 5000; 
    private String miIp = "127.0.0.1";
    private String idPuesto; 
    private int miPuertoLocal;
    private String clienteActual = null;
    private boolean reintentosAgotados = false;
    
    public OperadorModelo() {}

  
    public void configurarPuesto(String nroPuesto) {
        this.idPuesto = nroPuesto;
        try {
            this.miIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.miIp = "127.0.0.1";
        }
    }

   
    public boolean registrarEnServidor() {
        this.miPuertoLocal = 6000 + Integer.parseInt(this.idPuesto); 
        try (Socket s = new Socket(ipServidor, puertoServidor); 
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            
            String mensajeRegistro = Protocolo.CMD_REGISTRO + Protocolo.SEPARADOR + 
                                     this.miIp + Protocolo.SEPARADOR + 
                                     this.idPuesto + Protocolo.SEPARADOR + 
                                     this.miPuertoLocal;
            
            out.println(mensajeRegistro);
            String respuesta = in.readLine();
            
            if (Protocolo.OK_REGISTRADO.equals(respuesta)) {
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
    
    public String llamarSiguiente() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            out.println(Protocolo.CMD_LLAMAR + Protocolo.SEPARADOR + this.idPuesto);
            String res = in.readLine();

            if (res != null && !res.startsWith("ERROR") && !res.equals(Protocolo.ERR_FILA_VACIA)) {
                this.clienteActual = res;
                this.reintentosAgotados = false;
            }
            return res;
        } catch (Exception e) {
            return Protocolo.ERR_CONEXION;
        }
    }
    public String getClienteActual() {
        return clienteActual;
    }
    public boolean isReintentosAgotados() {
    	return reintentosAgotados;
    }

    public String reLlamar() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            out.println(Protocolo.CMD_RELLAMAR + Protocolo.SEPARADOR + this.idPuesto);
            String respuesta= in.readLine();
            if (Protocolo.SIN_REINTENTOS.equals(respuesta)) {
                this.reintentosAgotados = true; 
            }
            return respuesta; 
        } catch (Exception e) {
            return Protocolo.ERR_CONEXION; 
        }
    }

    public String obtenerTamañoFila() {
        try (Socket s = new Socket(ipServidor, puertoServidor);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            out.println(Protocolo.CMD_INFO_FILA);
            return in.readLine(); 
        } catch (Exception e) {
            return "-"; 
        }
    }
}