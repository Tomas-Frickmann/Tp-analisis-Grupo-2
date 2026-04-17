package monitor;

import util.ConfigView;
import util.TipoConfig; 

public class MonitorMain {

    public static void main(String[] args) {

       
        ConfigView config = new ConfigView(TipoConfig.MONITOR);
        config.setVisible(true);

        if (config.fueConfirmado()) {
            String ipServidor = "localhost";
            int puertoServidor = config.getPuertoRemoto();
            MonitorControlador controlador = new MonitorControlador();
            MonitorVentana ventana = new MonitorVentana();
            controlador.setVentana(ventana);
            controlador.configurarRed(ipServidor, puertoServidor);           
            
        } else {
            System.exit(0); 
        }
    }
}