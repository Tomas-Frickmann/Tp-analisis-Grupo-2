package monitor;

import util.ConfigView;
import util.TipoConfig; 

public class MonitorMain {

    public static void main(String[] args) {

        ConfigView config = new ConfigView(TipoConfig.SOLO_RECEPTOR);
        config.setVisible(true);

        if (config.fueConfirmado()) {
            int puertoEscucha = config.getPuertoLocal();

            MonitorControlador controlador = new MonitorControlador();
            MonitorVentana ventana = new MonitorVentana();

            controlador.configurarRed(puertoEscucha);
            controlador.setVentana(ventana);

            ventana.setVisible(true);
        } else {
            System.exit(0); 
        }
    }
}