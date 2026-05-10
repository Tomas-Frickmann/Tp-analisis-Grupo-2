package operador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import util.ConfigServidor;
import util.Protocolo;
import util.GestorJson; // IMPORTANTE: Ahora usamos el gestor para leer el JSON

public class OperadorModelo {
    
    private ConfigServidor config;
    private String miIp = "127.0.0.1";
    private String idPuesto; 
    private int miPuertoLocal;
    private String clienteActual = null;
    private boolean reintentosAgotados = false;

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

    /**
     * Esta es la lógica central de reconexión dinámica.
     * En lugar de IPs fijas, pregunta al JSON quién es el líder actual.
     */
    private String comunicacionConReitento(String comando) {
        int intentosRestantes = config.getMaxIntentosFallidos();

        while (intentosRestantes > 0) {
            // 1. Buscamos en el archivo JSON quién está marcado como Principal y Activo
            String[] datosPrincipal = GestorJson.obtenerPrincipalActivo();
            
            if (datosPrincipal != null) {
                String ipActual = datosPrincipal[0];
                int puertoActual = Integer.parseInt(datosPrincipal[1]);

                try {
                    // 2. Intentamos la conexión con el líder detectado
                    String respuesta = conectarYEnviar(ipActual, puertoActual, comando);
                    
                    // 3. Manejo de "Amnesia": Si el servidor no tiene registrado este puesto
                    if (Protocolo.ERR_PUESTO_NO_EXISTE.equals(respuesta) && !comando.startsWith(Protocolo.CMD_REGISTRO)) {
                        System.out.println("[SISTEMA] El servidor no reconoce el puesto. Registrando...");
                        registrarEnServidor(); // Auto-registro
                        return conectarYEnviar(ipActual, puertoActual, comando); // Reintento
                    }
                    
                    return respuesta; // Éxito total

                } catch (Exception e) {
                    // Si el socket falla (ej: el servidor murió justo ahora)
                    System.err.println("[RED] El Principal del JSON (" + puertoActual + ") no responde físicamente.");
                }
            } else {
                // Si el JSON no devuelve a nadie, es porque el Principal cayó 
                // y los Respaldos todavía están en el proceso de ascenso.
                System.out.println("[SISTEMA] No se encuentra un líder activo en el JSON. Esperando ascenso...");
            }

            // Bajamos una vida y esperamos un momento antes de volver a consultar el JSON
            intentosRestantes--;
            try { 
                Thread.sleep(2000); // Pausa para dar tiempo a que un respaldo suba a principal
            } catch (InterruptedException ie) { 
                Thread.currentThread().interrupt(); 
            }
        }

        System.out.println("ERROR FATAL: No se pudo contactar con ningún servidor líder tras varios intentos.");
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
        return Protocolo.OK_REGISTRADO.equals(respuesta);
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
        if (Protocolo.SIN_REINTENTOS.equals(respuesta)) this.reintentosAgotados = true; 
        return respuesta; 
    }

    public String obtenerTamañoFila() {
        String res = comunicacionConReitento(Protocolo.CMD_INFO_FILA);
        return res.equals(Protocolo.ERR_CONEXION) ? "-" : res;
    }

    public void desconectarDelServidor() {
        if (this.idPuesto != null) {
            comunicacionConReitento(Protocolo.CMD_DESCONECTAR + Protocolo.SEPARADOR + this.idPuesto);
        }
    }

    public String getClienteActual() { return clienteActual; }
    public boolean isReintentosAgotados() { return reintentosAgotados; }
}
