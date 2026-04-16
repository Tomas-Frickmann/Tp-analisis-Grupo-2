package operador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import interfaces.IVentana;

public class OperadorVentana extends JFrame implements IVentana{
    private static final long serialVersionUID = 1L;
    private JLabel lblContador = new JLabel("Clientes en espera: 0", SwingConstants.CENTER);
    private JLabel lblEstado = new JLabel("---", SwingConstants.CENTER);
    private JButton btnSiguiente;
    private JPanel mainPanel;
    private JPanel infoPanel;
    private ActionListener actionListener;
    

    public OperadorVentana() {        
    setTitle("PUESTO DE ATENCIÓN - CONTROL DE FILA");
    setSize(450, 400);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    // Colores gama verde
    Color verdeFondo = new Color(240, 248, 240);
    Color verdeBoton = new Color(67, 160, 71);
    Color grisTexto = new Color(55, 71, 79);

    mainPanel = new JPanel(new BorderLayout(20, 20));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    mainPanel.setBackground(verdeFondo);
    
    
    setContentPane(mainPanel);

    // Indicador de Fila
    infoPanel = new JPanel(new GridLayout(2, 1));
    infoPanel.setOpaque(false);
    
    lblEstado = new JLabel("SISTEMA ACTIVO", SwingConstants.CENTER);
    lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
    lblEstado.setForeground(verdeBoton);
    
    lblContador = new JLabel("Clientes en espera: 0", SwingConstants.CENTER);
    lblContador.setFont(new Font("Arial", Font.BOLD, 24));
    lblContador.setForeground(grisTexto);
    
    infoPanel.add(lblEstado);
    infoPanel.add(lblContador);
    mainPanel.add(infoPanel, BorderLayout.NORTH);

    // Botón Llamar
    this.btnSiguiente = new JButton("LLAMAR_SIGUIENTE");
    this.btnSiguiente.setActionCommand("LLAMAR_SIGUIENTE");
  
    this.btnSiguiente.setFont(new Font("Arial", Font.BOLD, 20));
    this.btnSiguiente.setBackground(verdeBoton);
    this. btnSiguiente.setForeground(Color.WHITE);
    this. btnSiguiente.setFocusable(false);
    this.btnSiguiente.setBorder(BorderFactory.createRaisedBevelBorder());

   
    mainPanel.add(btnSiguiente, BorderLayout.CENTER);

    
    setLocationRelativeTo(null);
    setVisible(false);
}
   public void setActionListener(ActionListener controlador) {
		this.actionListener=controlador;
		this.btnSiguiente.addActionListener(controlador);
	}
   
   public void mostrarMensaje(String mensaje,String titulo,int tipo) {
	   
	   JOptionPane.showMessageDialog(this,mensaje,titulo,tipo);
   }
   public void actualizarUI(String tamaño) {
       SwingUtilities.invokeLater(() -> {
           lblContador.setText("Clientes en espera: " + tamaño);
           lblEstado.setText("SISTEMA ACTIVO");
           lblEstado.setForeground(new Color(67, 160, 71));
       });
   }
public void cambiaEstado(String text) {
	 this.lblEstado.setText(text);
     this.lblEstado.setForeground(Color.RED);
	
}

	
	
	}
