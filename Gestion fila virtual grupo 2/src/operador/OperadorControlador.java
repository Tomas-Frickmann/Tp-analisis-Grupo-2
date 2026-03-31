package operador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import interfaces.IFilaListener;

public class OperadorControlador implements ActionListener, IFilaListener{
	
	private OperadorVentana ventana;
	private OperadorModelo modelo;
	
	
	public  OperadorControlador() {
	this.ventana=null;
	this.modelo=new OperadorModelo();
	this.modelo.setListener(this);
	
	}
	
	public void setVentana(OperadorVentana ventana) {
		this.ventana = ventana;
		this.ventana.setActionListener(this);
		configurarEventosVentana();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("LLAMAR_SIGUIENTE")) {
			this.llamarSiguiente();
			
			
		}
	}
	private void llamarSiguiente() {
	String c=this.modelo.llamarSiguiente();
	if (c.equalsIgnoreCase("VACIO")) 
		this.ventana.mostrarMensaje("No hay clientes en espera.", "FILA VACÍA", JOptionPane.INFORMATION_MESSAGE);
	else if (c.equalsIgnoreCase("EXITO")) {
		
		this.ventana.actualizarUI((int) this.modelo.tamañoFila()); 
	 this.ventana.mostrarMensaje("Siguiente cliente llamado con exito.","", JOptionPane.INFORMATION_MESSAGE);
	}
	 else{
		 this.ventana.cambiaEstado();
		 this.alOcurrirError("El monitor no se encuentra activo", "Error");
	 }
	
     

		 
		
	 

		
	}

	@Override
    public void alCambiarFila(int nuevoTamano) {
		SwingUtilities.invokeLater(() -> {
	    
	        this.ventana.actualizarUI(nuevoTamano);
         
	    });
           
    }

    @Override
    public void alOcurrirError(String mensaje, String titulo) {
    	SwingUtilities.invokeLater(() -> {
          
    		this.ventana.mostrarMensaje(mensaje,titulo,JOptionPane.ERROR_MESSAGE);
        });
           
       
    }
    

    private void configurarEventosVentana() {
        
        this.ventana.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {                
                modelo.desconectar(true);
                System.exit(0); 
            }
        });
    }

	public void configurarRed(String ipMonitor, int puertoMonitor, int puertoLocal) {
		// TODO Auto-generated method stub
		this.modelo.setMonitorIP_purto(ipMonitor,puertoMonitor);
		this.modelo.setPuertoServidor(puertoLocal);
		this.modelo.iniciarServidor();
	}

}