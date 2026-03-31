package cliente;

import util.ConfigView;
import util.TipoConfig;

public class ClienteMain {

    public static void main(String[] args) {
        
    	ConfigView config = new ConfigView(TipoConfig.SOLO_EMISOR);
        config.setVisible(true);
      
        if (config.fueConfirmado()) {
            ClienteView ventana = new ClienteView();
            ClienteController controlador = new ClienteController();
            controlador.configurarRed(config.getIpRemota(), config.getPuertoRemoto());
            controlador.setVentana(ventana);
            ventana.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}