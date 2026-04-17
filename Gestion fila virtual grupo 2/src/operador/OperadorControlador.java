package operador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import util.Protocolo; // <-- IMPORTAMOS LAS CONSTANTES

public class OperadorControlador implements ActionListener {
	
	private OperadorVentana ventana;
	private OperadorModelo modelo;
	private Timer timerActualizacion; 
	private Timer timerCooldownRellamar;	
	public OperadorControlador() {
		this.ventana = null;
		this.modelo = new OperadorModelo();
	}
	
	public void setVentana(OperadorVentana ventana) {
		this.ventana = ventana;
		this.ventana.setActionListener(this);
		iniciarRelojDeFila(); 
	}

	private void iniciarRelojDeFila() {
		
		timerActualizacion = new Timer(500, e -> {
			String cantidad = this.modelo.obtenerTamañoFila();
			ventana.actualizarUI(cantidad); 
		});
		timerActualizacion.start();
	}

	@Override
    public void actionPerformed(ActionEvent e) {
        String comandoStr = e.getActionCommand();
        if (comandoStr.equals(Protocolo.CMD_LLAMAR)) {
            this.llamarSiguiente();
        } 
        else if (comandoStr.equals(Protocolo.CMD_RELLAMAR)) {
            this.reLlamar();           
        }
    }
	private void reLlamar() {
		 String respuesta = this.modelo.reLlamar();
	
		if (Protocolo.SIN_REINTENTOS.equals(respuesta)) {
            this.ventana.mostrarMensaje("El cliente se quedo sin reintentos. /n Por favor llame al siguiente", "REINTENTOS AGOTADOS", JOptionPane.INFORMATION_MESSAGE);
        } 
        else if (respuesta != null && respuesta.startsWith("ERROR")) {
            this.ventana.cambiaEstado("Error de conexión");
        } 
    }
	private void llamarSiguiente() {
        String respuesta = this.modelo.llamarSiguiente();

        if (Protocolo.ERR_FILA_VACIA.equals(respuesta)) {
            this.ventana.mostrarMensaje("No hay clientes en espera.", "FILA VACÍA", JOptionPane.INFORMATION_MESSAGE);
        } 
        else if (respuesta != null && respuesta.startsWith("ERROR")) {
            this.ventana.cambiaEstado("Error de conexión");
        } 
        else {
            this.ventana.actualizarDniAtendiendo(respuesta); 
            this.iniciarCooldownRellamar(); 
        }
     }

	public boolean registrarEnServidor() {
		boolean respuesta = this.modelo.registrarEnServidor();
		if (respuesta) {
		    this.ventana.mostrarMensaje("Registrado exitosamente en el servidor central.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
		} else {
			this.ventana.mostrarMensaje("El Servidor Central rechazó la conexión o no está activo.", "Error de Red", JOptionPane.ERROR_MESSAGE);
		}
	
		return respuesta;
	}

	public void configurarPuesto(String idPuesto) {
		this.modelo.configurarPuesto(idPuesto);
	}
	private void iniciarCooldownRellamar() {
       
        this.ventana.setBotonRellamarActivo(false);
        if (timerCooldownRellamar != null && timerCooldownRellamar.isRunning()) {
            timerCooldownRellamar.stop();
        }
        timerCooldownRellamar = new Timer(30000, e -> {
           this.ventana.setBotonRellamarActivo(true);
        });
        timerCooldownRellamar.setRepeats(false);
        timerCooldownRellamar.start();
    }
}