package cliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

public class ClienteController implements ActionListener{
	private ClienteView view;
	private ClienteModel model;
	
	
	
	public ClienteController() {
		super();
		
		
	
		
    }

    private void configurarEventosVentana() {
        
        this.view.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {                
                model.desconectar();
                System.exit(0); 
            }
        });
    }


	public void setVentana(ClienteView ventana) {
		// TODO Auto-generated method stub
		this.view=ventana;
		this.view.setActionListener(this);
		configurarEventosVentana();
	}
	@Override
	
	
   public void actionPerformed(ActionEvent e) {
	        String currentText = this.view.getTextDni();
	        String key = e.getActionCommand();

	        if (key.equals("CLR")) {
	        	
	            this.view.setTextDni("");
	        } else if (key.equals("<-")) {
	            if (!currentText.isEmpty()) {
	            	
	            	this.view.setTextDni(currentText.substring(0, currentText.length() - 1)); 
	            }
	        } 
	        else if(key.equals("OBTENER MI TURNO") ){
	        	if (this.dniValido(currentText)){
	        		currentText = currentText.trim();
	        			if (this.model.enviarTurnoPorSocket(currentText)) {
	        				this.view.mostrarMensaje( "Registro Exitoso. \nSu turno ha sido añadido.", "TURNO CONFIRMADO", JOptionPane.INFORMATION_MESSAGE);
	        			this.view.setTextDni("");
	        			}
	        			else
	        				this.view.mostrarMensaje("Error de Red: No se pudo conectar con el Operador.", "ERROR DE CONEXIÓN", JOptionPane.ERROR_MESSAGE);
	        	}
	        	else
	        		this.view.mostrarMensaje( "El DNI ingresado es inválido.\nPor favor, verifique.", "DNI INVÁLIDO", JOptionPane.WARNING_MESSAGE);
	        
	        }
	        
	        else {
	            
	            if (currentText.length() < 9) {
	                this.view.setTextDni(currentText+key);
	            }
	        }
	    }



	private boolean dniValido(String dni) {
		
		return !dni.isEmpty() && dni.matches("\\d{6,9}");
	}

	public void configurarRed(String ipDestino, int puertoDestino) {
		this.model= new ClienteModel(ipDestino,puertoDestino);
		
		
		
	}
	

}

	