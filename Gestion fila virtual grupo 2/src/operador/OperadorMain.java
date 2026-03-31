package operador;

import util.ConfigView;
import util.TipoConfig;

public class OperadorMain {
    public static void main(String[] args) {
        ConfigView config = new ConfigView(TipoConfig.EMISOR_RECEPTOR);
        config.setVisible(true);

        if (config.fueConfirmado()) {
         
            String ipMonitor = config.getIpRemota();
            int puertoMonitor = config.getPuertoRemoto();
            int puertoLocal = config.getPuertoLocal();

            OperadorControlador controlador = new OperadorControlador();
            OperadorVentana ventana = new OperadorVentana();
            
            controlador.configurarRed(ipMonitor, puertoMonitor, puertoLocal);
            controlador.setVentana(ventana);
            ventana.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}