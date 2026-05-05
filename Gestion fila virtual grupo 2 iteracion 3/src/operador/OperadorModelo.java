package operador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import util.ConfigServidor;
import util.Protocolo;

public class OperadorModelo {
    
    private ConfigServidor config;
    private String miIp = "127.0.0.1";
    private String idPuesto; 
    private int miPuertoLocal;
    private String clienteActual = null;
    private boolean reintentosAgotados = false;
    private boolean usarRespaldo = false; 
    public OperadorModelo() {
        this.config = new ConfigServidor("config_servidores.properties");
    }

    public void configurarPuesto(String nroPuesto) {
        this.idPuesto = nroPuesto;
        try {
            this.miIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.miIp = "127.0.0.1";
        }
        this.miPuertoLocal = 6000 + Integer.parseInt(this.idPuesto); 
    }

    
 

    private String comunicacionConReitento(String comando) {
        int intentosRestantes = config.getMaxIntentosFallidos();

        while (intentosRestantes > 0) {
           
            String ipActual = usarRespaldo ? config.getIpRespaldo() : config.getIpPrincipal();
            int puertoActual = usarRespaldo ? config.getPuertoRespaldo() : config.getPuertoPrincipal();
            
            try {
                
                return conectarYEnviar(ipActual, puertoActual, comando);
                
                
            } 
            catch (Exception e1) {
                
                usarRespaldo = !usarRespaldo; 
                String nuevoObjetivo = usarRespaldo ? "Respaldo" : "Principal";
                System.out.println(" Servidor actual no responde. Cambiando ruta permanentemente a " + nuevoObjetivo + "...");
                
                
                ipActual = usarRespaldo ? config.getIpRespaldo() : config.getIpPrincipal();
                puertoActual = usarRespaldo ? config.getPuertoRespaldo() : config.getPuertoPrincipal();
                
                try {
                    return conectarYEnviar(ipActual, puertoActual, comando);
                } 
                catch (Exception e2) {
                   
                    intentosRestantes--;
                    System.out.println(" Ambos servidores caídos. Intentos restantes: " + intentosRestantes);
                    
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
        System.out.println(" ERROR FATAL: Todo el sistema está offline.");
        return Protocolo.ERR_CONEXION; 
    }

    
    private String conectarYEnviar(String ip, int puerto, String comando) throws Exception {
        try (Socket s = new Socket(ip, puerto);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
             
            out.println(comando);
            return in.readLine();
        }
    }



    public boolean registrarEnServidor() {
        String mensajeRegistro = Protocolo.CMD_REGISTRO + Protocolo.SEPARADOR + 
                                 this.miIp + Protocolo.SEPARADOR + 
                                 this.idPuesto + Protocolo.SEPARADOR + 
                                 this.miPuertoLocal;
        
        String respuesta = comunicacionConReitento(mensajeRegistro);
        
        if (Protocolo.OK_REGISTRADO.equals(respuesta)) {
            System.out.println("Puesto " + this.idPuesto + " registrado exitosamente.");
            return true;
        } else {
            System.out.println("El servidor rechazó el registro: " + respuesta);
            return false;
        }  
    }
    
    public String llamarSiguiente() {
        String comando = Protocolo.CMD_LLAMAR + Protocolo.SEPARADOR + this.idPuesto;
        String res = comunicacionConReitento(comando);

        if (!res.equals(Protocolo.ERR_CONEXION) && !res.startsWith("ERROR") && !res.equals(Protocolo.ERR_FILA_VACIA)) {
            this.clienteActual = res;
            this.reintentosAgotados = false;
        }
        return res;
    }

    public String reLlamar() {
        String comando = Protocolo.CMD_RELLAMAR + Protocolo.SEPARADOR + this.idPuesto;
        String respuesta = comunicacionConReitento(comando);
        
        if (Protocolo.SIN_REINTENTOS.equals(respuesta)) {
            this.reintentosAgotados = true; 
        }
        return respuesta; 
    }

    public String obtenerTamañoFila() {
        String comando = Protocolo.CMD_INFO_FILA;
        String res = comunicacionConReitento(comando);
        
        if (res.equals(Protocolo.ERR_CONEXION)) {
            return "-"; 
        }
        return res;
    }

    public String getClienteActual() {
        return clienteActual;
    }

    public boolean isReintentosAgotados() {
        return reintentosAgotados;
    }

    
    public void desconectarDelServidor() {
        if (this.idPuesto != null) {
            String comando = Protocolo.CMD_DESCONECTAR + Protocolo.SEPARADOR + this.idPuesto;
            comunicacionConReitento(comando);
        }
    }
}