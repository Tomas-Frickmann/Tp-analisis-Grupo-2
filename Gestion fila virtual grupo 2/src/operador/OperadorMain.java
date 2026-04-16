package operador;

import javax.swing.JOptionPane;
import util.ConfigView;
import util.TipoConfig;

public class OperadorMain {
    public static void main(String[] args) {
        ConfigView config = new ConfigView(TipoConfig.OPERADOR);
        config.setVisible(true);

        if (config.fueConfirmado()) {
            // Obtenemos el puerto que el usuario ingresó
            String puertoLocal = config.getPuesto(); 
            
            // Lo convertimos a String porque nuestro protocolo usa Strings
            String idPuesto = String.valueOf(puertoLocal);

            OperadorControlador controlador = new OperadorControlador();
            OperadorVentana ventana = new OperadorVentana();
            
            controlador.setVentana(ventana);
            // 1. LE PASAMOS EL ID AL SISTEMA (¡Esto es lo que faltaba!)
            controlador.configurarPuesto(idPuesto);

            // 2. AHORA SÍ, INTENTAMOS REGISTRARNOS
            boolean registrado = controlador.registrarEnServidor();
            
            if (registrado) {
               
                // Le pasamos el ID a la ventana por si querés mostrar "Puesto: 5001" en la UI
                ventana.setTitle("PUESTO DE ATENCIÓN - Puesto " + idPuesto); 
                ventana.setVisible(true);
            } else {
                
                System.exit(0);
            }
            
        } else {
            System.exit(0);
        }
    }
}