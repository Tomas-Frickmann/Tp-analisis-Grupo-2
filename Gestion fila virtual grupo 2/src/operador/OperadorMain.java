package operador;

import util.ConfigView;
import util.TipoConfig;

public class OperadorMain {
    public static void main(String[] args) {
        ConfigView config = new ConfigView(TipoConfig.OPERADOR);
        config.setVisible(true);

        if (config.fueConfirmado()) {
          
            String puertoLocal = config.getPuesto();
            String idPuesto = String.valueOf(puertoLocal);
            OperadorControlador controlador = new OperadorControlador();
            OperadorVentana ventana = new OperadorVentana();
            controlador.setVentana(ventana);
            controlador.configurarPuesto(idPuesto);
            boolean registrado = controlador.registrarEnServidor();
            
            if (registrado) {
                ventana.setTitle("PUESTO "+idPuesto+" DE ATENCIÓN"); 
                ventana.setVisible(true);
            } else {
                System.exit(0);
            }
            
        } else {
            System.exit(0);
        }
    }
}