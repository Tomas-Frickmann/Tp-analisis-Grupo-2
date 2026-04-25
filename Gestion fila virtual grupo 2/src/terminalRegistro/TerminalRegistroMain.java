package terminalRegistro;

import util.ConfigView;
import util.TipoConfig;

public class TerminalRegistroMain {

    public static void main(String[] args) {
        
    	ConfigView config = new ConfigView(TipoConfig.KIOSCO);
        config.setVisible(true);
      
        if (config.fueConfirmado()) {
            TerminalRegistroVista ventana = new TerminalRegistroVista();
            TerminalRegistroControlador controlador = new TerminalRegistroControlador();
            controlador.configurarRed(config.getIpRemota(), config.getPuertoRemoto());
            controlador.setVentana(ventana);
            ventana.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}