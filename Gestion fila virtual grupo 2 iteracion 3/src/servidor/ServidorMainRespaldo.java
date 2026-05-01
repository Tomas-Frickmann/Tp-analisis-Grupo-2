package servidor;
import util.ConfigServidor;

public class ServidorMainRespaldo{
    public static void main(String[] args) {
        
    	ConfigServidor config = new ConfigServidor("config_servidores.properties");
        
        
        ServidorLogic logica = new ServidorLogic(config, true);
        logica.iniciarServidor();
    }
}