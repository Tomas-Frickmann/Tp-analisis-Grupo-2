package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigServidor {
    private String ipPrincipal;
    private int puertoPrincipal;
    private String ipRespaldo;
    private int puertoRespaldo;
    private int intervaloPing;
    private int maxIntentosFallidos;
    private String algoritmoCifrado;
    private String claveSecreta;
    private String formatoPersistencia;
    private String archivoPersistencia;

    public ConfigServidor(String rutaArchivo) {
        Properties prop = new Properties();
        
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            prop.load(fis);
            
            this.ipPrincipal = prop.getProperty("principal.ip", "localhost");
            this.puertoPrincipal = Integer.parseInt(prop.getProperty("principal.puerto", "5000"));
            
            this.ipRespaldo = prop.getProperty("respaldo.ip", "localhost");
            this.puertoRespaldo = Integer.parseInt(prop.getProperty("respaldo.puerto", "5010"));
            
            this.intervaloPing = Integer.parseInt(prop.getProperty("intervaloPing", "2000"));
            this.maxIntentosFallidos = Integer.parseInt(prop.getProperty("maxIntentosFallidos", "3"));

           
            this.algoritmoCifrado = prop.getProperty("seguridad.algoritmo", "XOR");
            this.claveSecreta = prop.getProperty("seguridad.clave", "SistemadeGestionAyDS2");
            
            this.formatoPersistencia = prop.getProperty("persistencia.formato", "JSON");
            this.archivoPersistencia = prop.getProperty("persistencia.archivo", "historial_filas");

        } catch (IOException | NumberFormatException e) {
            System.err.println("No se encontró el archivo .properties o hay un error de formato. Usando valores por defecto.");
            this.ipPrincipal = "localhost"; this.puertoPrincipal = 5000;
            this.ipRespaldo = "localhost"; this.puertoRespaldo = 5010;
            this.intervaloPing = 2000; this.maxIntentosFallidos = 3;
            
           
            this.algoritmoCifrado = "XOR";
            this.claveSecreta = "ClavePredeterminada";
        }
    }

    public String getIpPrincipal() { 
        return ipPrincipal; 
    }
    public int getPuertoPrincipal() { 
        return puertoPrincipal; 
    }
    public String getIpRespaldo() { 
        return ipRespaldo; 
    }
    public int getPuertoRespaldo() { 
        return puertoRespaldo; 
    }
    public int getintervaloPing() { 
        return intervaloPing; 
    }
    public int getMaxIntentosFallidos() { 
        return maxIntentosFallidos; 
    }
    
    
    public String getAlgoritmoCifrado() {
        return algoritmoCifrado;
    }
    public String getClaveSecreta() {
        return claveSecreta;
    }

    public void setPuertoRespaldo(int puertoRespaldo) {
        this.puertoRespaldo = puertoRespaldo;
    }
    public void setIpPrincipal(String ip) {
        this.ipPrincipal = ip;
    }
    public void setPuertoPrincipal(int puerto) {
        this.puertoPrincipal = puerto;
    }
    public String getFormatoPersistencia() {
    	return formatoPersistencia;
    }
    public String getArchivoPersistencia(){
    	return archivoPersistencia;
    }
}