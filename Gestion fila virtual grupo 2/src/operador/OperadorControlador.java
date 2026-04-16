package operador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class OperadorControlador implements ActionListener{
	
	private OperadorVentana ventana;
	private OperadorModelo modelo;
	private Timer timerActualizacion; 
		
	public  OperadorControlador() {
	this.ventana=null;
	this.modelo=new OperadorModelo();
	
	
	}
	
	public void setVentana(OperadorVentana ventana) {
		this.ventana = ventana;
		this.ventana.setActionListener(this);
		iniciarRelojDeFila(); // Lo arrancamos apenas tenemos la ventana
		
	}
	private void iniciarRelojDeFila() {
        // Se ejecuta cada 2000 milisegundos (2 segundos)
        timerActualizacion = new Timer(500, e -> {
            // Con la lambda, 'this' sigue siendo el Controlador
            String cantidad = this.modelo.obtenerTamañoFila();
            
            ventana.actualizarUI(cantidad); 
        });
        timerActualizacion.start();
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("LLAMAR_SIGUIENTE")) {
			this.llamarSiguiente();
			
			
		}
	}
	private void llamarSiguiente() {
		String respuesta = this.modelo.llamarSiguiente();

		if (respuesta.equals("ERROR:FILA_VACIA")) {
		    this.ventana.mostrarMensaje("No hay clientes en espera.", "FILA VACÍA", JOptionPane.INFORMATION_MESSAGE);
		} else if (respuesta.startsWith("ERROR")) {
		    this.ventana.cambiaEstado("Error de conexión");
		} else {
		    
		    System.out.println("cliente llamado: " + respuesta);
		}
	 }
	
     

		 
		
	 

		
	

	
    

	

	public boolean registrarEnServidor() {
		boolean respuesta=this.modelo.registrarEnServidor();
		if ( respuesta) {
			this.ventana.mostrarMensaje("Registrado exitosamente en el servidor central.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
			
		}
		else {
			this.ventana.mostrarMensaje( "El Servidor Central rechazó la conexión o no está activo.", "Error de Red", JOptionPane.ERROR_MESSAGE);
		}
	
		return respuesta;
	}

	public void configurarPuesto(String idPuesto) {
		this.modelo.configurarPuesto(idPuesto);
		
	}

}