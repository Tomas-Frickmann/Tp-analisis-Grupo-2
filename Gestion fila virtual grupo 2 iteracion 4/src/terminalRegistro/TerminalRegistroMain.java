
package terminalRegistro;

public class TerminalRegistroMain {


        
    	public static void main(String[] args) {
            System.out.println("Iniciando terminal Kiosco...");
            TerminalRegistroControlador controlador = new TerminalRegistroControlador();
            
           
            TerminalRegistroVista vista = new TerminalRegistroVista();
            controlador.setVentana(vista);
            vista.setVisible(true);
        }
    }
