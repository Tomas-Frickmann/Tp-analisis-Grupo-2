package terminalRegistro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import util.Protocolo;

public class TerminalRegistroControlador implements ActionListener {
    private TerminalRegistroVista view;
    private TerminalRegistroModelo model;

    public void setVentana(TerminalRegistroVista ventana) {
        this.view = ventana;
        this.view.setActionListener(this);
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
        else if (key.equals("OBTENER MI TURNO")) {
            ejecutarRegistroTurno(currentText);
        } 
        else {
            if (currentText.length() < 9) {
                this.view.setTextDni(currentText + key);
            }
        }
    }

    private void ejecutarRegistroTurno(String dni) {
        if (dni.isEmpty() ){
        	
            this.view.mostrarMensaje("El DNI no puede estar vacio.", "DNI INVÁLIDO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dni.length() < 6) {
        	
        	this.view.mostrarMensaje("El DNI debe tener por lo menos 6 digitos.", "DNI INVÁLIDO", JOptionPane.WARNING_MESSAGE);
			return;
		}

        
        String respuesta = this.model.enviarTurnoPorSocket(dni.trim());

        if (Protocolo.OK_CLIENTE_CREADO.equals(respuesta)) {
            this.view.mostrarMensaje("Registro Exitoso. \nSu turno ha sido añadido.", "TURNO CONFIRMADO", JOptionPane.INFORMATION_MESSAGE);
            this.view.setTextDni("");
        } 
        else if (Protocolo.ERR_DNI_DUPLICADO.equals(respuesta)) {
            this.view.mostrarMensaje("Este DNI ya se encuentra en la fila de espera.", "TURNO DUPLICADO", JOptionPane.WARNING_MESSAGE);
            this.view.setTextDni("");
        } 
        else {
            this.view.mostrarMensaje("Error de Red: No se pudo conectar con el Servidor Central.", "ERROR", JOptionPane.ERROR_MESSAGE);
            this.view.setTextDni("");
        }
    }

    private boolean dniValido(String dni) {
        return !dni.isEmpty() && dni.matches("\\d{6,9}");
    }

    public void configurarRed(String ipDestino, int puertoDestino) {
        this.model = new TerminalRegistroModelo(ipDestino, puertoDestino);
    }
}