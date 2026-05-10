package operador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import util.Protocolo; 

public class OperadorControlador implements ActionListener {
	
	private OperadorVentana ventana;
	private OperadorModelo modelo;
	private Timer timerActualizacion; 
	private Timer timerCooldownRellamar;	
    private boolean actualizandoFila = false;

	public OperadorControlador() {
		this.ventana = null;
		this.modelo = new OperadorModelo();
		this.timerCooldownRellamar = new Timer(30000, e -> {
	        this.ventana.setBotonRellamarActivo(true);
	    });
	    this.timerCooldownRellamar.setRepeats(false);
	}
	
	public void setVentana(OperadorVentana ventana) {
		this.ventana = ventana;
		this.ventana.setActionListener(this);
		iniciarRelojDeFila(); 
        
        
        if (this.ventana instanceof JFrame) {
            JFrame frame = (JFrame) this.ventana;
            
            
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Cerrando puesto... Avisando al servidor.");
                    
                    
                    if (timerActualizacion != null) timerActualizacion.stop();
                    if (timerCooldownRellamar != null) timerCooldownRellamar.stop();
                    
                    
                    modelo.desconectarDelServidor();
                    
                    
                    System.exit(0);
                }
            });
        }
	}

	
 
    private void iniciarRelojDeFila() {
        timerActualizacion = new Timer(500, e -> {
            
            if (!actualizandoFila) {
                actualizandoFila = true;
                
                
                new Thread(() -> {
                    String cantidad = this.modelo.obtenerTamañoFila();
                    
                    
                    SwingUtilities.invokeLater(() -> {
                        ventana.actualizarUI(cantidad);
                        actualizandoFila = false; 
                    });
                }).start();
            }
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
	    // 1. Bloqueamos el botón inmediatamente para que no haga doble clic por ansiedad
	    this.ventana.setBotonRellamarActivo(false); 

	    // 2. Mandamos el trabajo de red a un hilo de fondo para no congelar la pantalla
	    new Thread(() -> {
	        String respuesta = this.modelo.reLlamar();
	        
	        // 3. Volvemos al hilo de la interfaz para actualizar los carteles
	        SwingUtilities.invokeLater(() -> {
	            if (Protocolo.SIN_REINTENTOS.equals(respuesta)) {
	                this.ventana.mostrarMensaje("Reintentos agotados.", "AVISO", JOptionPane.WARNING_MESSAGE);
	                this.ventana.actualizarDniAtendiendo("Nadie");
	            } 
	            else if (respuesta != null && respuesta.startsWith("ERROR")) {
	                this.ventana.cambiaEstado("Error de conexión");
	                this.ventana.setBotonRellamarActivo(true); // Lo volvemos a activar para que reintente luego
	            }
	            else {
	                this.iniciarCooldownRellamar();
	            }
	        });
	    }).start();
	}
    private void llamarSiguiente() {
        boolean hayAlguien = (this.modelo.getClienteActual() != null);
        boolean tieneReintentos = !this.modelo.isReintentosAgotados();
        boolean timerCorriendo = (this.timerCooldownRellamar != null && this.timerCooldownRellamar.isRunning());

        this.ventana.setBotonRellamarActivo(false); 

        new Thread(() -> {
            String respuesta = this.modelo.llamarSiguiente();

            SwingUtilities.invokeLater(() -> {
                if (Protocolo.ERR_FILA_VACIA.equals(respuesta)) {
                    this.ventana.mostrarMensaje("No hay clientes en espera.", "FILA VACÍA", JOptionPane.INFORMATION_MESSAGE);

                    if (hayAlguien && tieneReintentos && !timerCorriendo) {
                        this.ventana.setBotonRellamarActivo(true);
                    } else {
                        this.ventana.setBotonRellamarActivo(false);
                    }
                } 
                else if (respuesta != null && respuesta.startsWith("ERROR")) {
                	System.out.println(respuesta);
                    this.ventana.cambiaEstado("Error de conexión");
                    this.ventana.setBotonRellamarActivo(hayAlguien && tieneReintentos && !timerCorriendo);
                } 
                else {
                    this.ventana.actualizarDniAtendiendo(respuesta); 
                    this.iniciarCooldownRellamar(); 
                }
            });
        }).start();
    }
    
	public boolean registrarEnServidor() {
		boolean respuesta = this.modelo.registrarEnServidor();
		if (respuesta) {
		    this.ventana.mostrarMensaje("Registrado exitosamente en el servidor central.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
		} else {
			this.ventana.mostrarMensaje("El Servidor Central rechazó la conexión, el puesto ya existe.", "Puesto Existente", JOptionPane.ERROR_MESSAGE);
		}
		return respuesta;
	}

	public void configurarPuesto(String idPuesto) {
		this.modelo.configurarPuesto(idPuesto);
	}
    
	private void iniciarCooldownRellamar() {
	    this.ventana.setBotonRellamarActivo(false);

	    if (this.timerCooldownRellamar != null) {
	        this.timerCooldownRellamar.restart();
	    }
	}
}