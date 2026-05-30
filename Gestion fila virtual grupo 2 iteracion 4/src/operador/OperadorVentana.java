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
import util.Protocolo; // <-- IMPORTAMOS LAS CONSTANTES

public class OperadorVentana extends JFrame implements IVentana {
    private static final long serialVersionUID = 1L;
    private JLabel lblContador;
    private JLabel lblEstado;
    private JLabel lblDNIActual;
    private JButton btnRellamar;
    private JButton btnSiguiente;
    
    public OperadorVentana() {        
        setTitle("PUESTO DE ATENCIÓN");
        setSize(500, 350); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Paleta de colores
        Color verdeFondo = new Color(240, 248, 240);
        Color verdeBoton = new Color(67, 160, 71);
        Color naranjaBoton = new Color(230, 115, 0); 
        Color grisTexto = new Color(55, 71, 79);

       
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(verdeFondo);
        setContentPane(mainPanel);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 10, 10)); 
        infoPanel.setOpaque(false);
        
        lblEstado = new JLabel("SISTEMA ACTIVO", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setForeground(verdeBoton);
        
        lblContador = new JLabel("Clientes en espera: 0", SwingConstants.CENTER);
        lblContador.setFont(new Font("Arial", Font.BOLD, 22)); 
        lblContador.setForeground(grisTexto);
        
        lblDNIActual = new JLabel("Atendiendo a: Nadie", SwingConstants.CENTER);
        lblDNIActual.setForeground(grisTexto);
        lblDNIActual.setFont(new Font("Arial", Font.BOLD, 26)); 
        
        infoPanel.add(lblEstado);
        infoPanel.add(lblContador);
        infoPanel.add(lblDNIActual);
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);         

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 20, 0)); 
        panelBotones.setOpaque(false);
        
        btnRellamar = new JButton("VOLVER A LLAMAR");
        btnRellamar.setForeground(Color.WHITE);
        btnRellamar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRellamar.setFocusable(false);
        btnRellamar.setBackground(naranjaBoton);
        btnRellamar.setActionCommand(Protocolo.CMD_RELLAMAR);
        btnRellamar.setEnabled(false);
        btnRellamar.setBackground(Color.GRAY);
        btnSiguiente = new JButton("LLAMAR SIGUIENTE");
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.setFont(new Font("Arial", Font.BOLD, 14));
        btnSiguiente.setFocusable(false);
        btnSiguiente.setBackground(verdeBoton);
        btnSiguiente.setActionCommand(Protocolo.CMD_LLAMAR); 
        
        panelBotones.add(btnRellamar);
        panelBotones.add(btnSiguiente);

        mainPanel.add(panelBotones, BorderLayout.SOUTH); 
        
        setVisible(false);
    }
    
    public void setActionListener(ActionListener controlador) {
    
        this.btnSiguiente.addActionListener(controlador);
        this.btnRellamar.addActionListener(controlador);
    }
    
    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }
    
    public void actualizarUI(String tamaño) {
        SwingUtilities.invokeLater(() -> {
            lblContador.setText("Clientes en espera: " + tamaño);
            lblEstado.setText("SISTEMA ACTIVO");
            lblEstado.setForeground(new Color(67, 160, 71));
        });
    }
    
    public void cambiaEstado(String text) {
        SwingUtilities.invokeLater(() -> {
            this.lblEstado.setText(text);
            this.lblEstado.setForeground(Color.RED);
        });
    }

   
    public void actualizarDniAtendiendo(String dni) {
        SwingUtilities.invokeLater(() -> {
            this.lblDNIActual.setText("Atendiendo a: " + dni);
        });
    }
    public void setBotonRellamarActivo(boolean activo) {

        SwingUtilities.invokeLater(() -> {
            btnRellamar.setEnabled(activo);
            if(activo) {
                btnRellamar.setBackground(new Color(230, 115, 0)); 
            } else {
                btnRellamar.setBackground(Color.GRAY); 
            }
        });
    }
}