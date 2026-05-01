package monitor;

import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import interfaces.IMonitorListener;

public class MonitorControlador implements IMonitorListener {
	
	private MonitorModelo modelo;
	private MonitorVentana ventana;
	
	
	//private LinkedList<String> historialTurnos;
	
	public MonitorControlador() {
		this.ventana = null;
		this.modelo = new MonitorModelo();
		this.modelo.setListener(this); 
		//this.historialTurnos = new LinkedList<>();
	}
	
	public void setVentana(MonitorVentana ventana) {
		this.ventana = ventana;
	}

	@Override
	public void alOcurrirError(String mensaje, String titulo) {
		SwingUtilities.invokeLater(() -> {
			this.ventana.mostrarMensaje(mensaje, titulo, JOptionPane.ERROR_MESSAGE);
			this.ventana.dispose(); 
		});
	}

	@Override
	public void alRecibirNuevoLlamado(LinkedList<String> dnis) {
		SwingUtilities.invokeLater(() -> {
			this.ventana.actualizarMonitor(dnis);
		});
	}

	
	
		public void configurarRed( ) {
			
			this.modelo.iniciarEscuchaPermanente(); 
			this.ventana.setVisible(true); 
		}
}