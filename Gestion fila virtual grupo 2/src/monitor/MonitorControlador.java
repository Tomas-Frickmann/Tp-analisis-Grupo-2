package monitor;

import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import interfaces.IMonitorListener;

public class MonitorControlador implements IMonitorListener{
	
 private MonitorModelo modelo;
 private MonitorVentana ventana;
 
public MonitorControlador()  {
	this.ventana=null;
	this.modelo= new MonitorModelo();
	this.modelo.setListener(this);

	

}
 
	public void setVentana(MonitorVentana ventana) {
		// TODO Auto-generated method stub
		this.ventana=ventana;
		
	}

	

	@Override
	public void alOcurrirError(String mensaje, String titulo) {
		SwingUtilities.invokeLater(() -> {
	          
    		this.ventana.mostrarMensaje(mensaje,titulo,JOptionPane.ERROR_MESSAGE);
        });
	}

	@Override
	public void alRecibirNuevoLlamado(LinkedList<String> dnis) {
		this.ventana.actualizarMonitor(dnis);
		// TODO Auto-generated method stub
		
	}

	public void configurarRed(int puertoEscucha) {
		
		this.modelo.setPuertoEscucha(puertoEscucha);
		this.modelo.iniciarServidor();
	}


}
