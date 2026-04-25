package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane; 
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ConfigView extends JDialog {
    private JTextField txtIpRemota, txtPuertoRemoto, txtPuertoLocal, txtPuesto;
    private boolean confirmado = false;
    private TipoConfig tipo;

    private final Color VERDE_PRIMARIO = new Color(46, 139, 87);   
    private final Color VERDE_OSCURO = new Color(34, 100, 60);   
    private final Color VERDE_FONDO = new Color(245, 255, 250);   
    private final Color VERDE_IP = new Color(0, 153, 76);         
    private final Color GRIS_TEXTO = new Color(50, 50, 50);       
    private final Color BORDE_CAMPO = new Color(200, 220, 200);   
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_CAMPO = new Font("Consolas", Font.PLAIN, 13); 

    public ConfigView(TipoConfig tipo) {
        this.tipo = tipo;

        setTitle("Inicializar Red - " + formatearTitulo(tipo));
        setModal(true);
        setAlwaysOnTop(true);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBorder(new EmptyBorder(25, 25, 25, 25)); 
        panelPrincipal.setBackground(VERDE_FONDO);

        JLabel lblTitulo = new JLabel("Configuración de Conexión");
        lblTitulo.setFont(FONT_TITULO);
        lblTitulo.setForeground(VERDE_PRIMARIO);
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(0, 2, 10, 20));
        panelForm.setBackground(VERDE_FONDO); 

        panelForm.add(crearLabelEstilizado(" Mi IP detectada:"));
        
        JPanel panelIpLocal = new JPanel(new BorderLayout());
        panelIpLocal.setBackground(VERDE_FONDO);
        JLabel lblIpLocal = new JLabel(obtenerIpLocal());
        lblIpLocal.setFont(FONT_CAMPO.deriveFont(Font.BOLD, 14f));
        lblIpLocal.setForeground(VERDE_IP);
        lblIpLocal.setHorizontalAlignment(JLabel.LEFT);
        panelIpLocal.add(lblIpLocal, BorderLayout.CENTER);
        
        panelForm.add(panelIpLocal);

        configurarCampos(panelForm);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        JButton btnConectar = crearBotonEstilizado("Confirmar y Conectar");
        
      
        btnConectar.addActionListener(e -> {
            if (validarCampos()) {
                confirmado = true;
                dispose();
            } else {
               
                JOptionPane.showMessageDialog(this, 
                    "Por favor, complete todos los campos antes de continuar.", 
                    "Campos Incompletos", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel panelBoton = new JPanel(new BorderLayout());
        panelBoton.setBackground(VERDE_FONDO);
        panelBoton.setBorder(new EmptyBorder(20, 0, 0, 0));
        panelBoton.add(btnConectar, BorderLayout.CENTER);
        
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        getContentPane().add(panelPrincipal);

        pack();
        
        if(getWidth() < 480) setSize(480, getHeight());
        
        setLocationRelativeTo(null);
    }

    
    private boolean validarCampos() {
        if (tipo == TipoConfig.KIOSCO || tipo == TipoConfig.MONITOR) {
         
            if (txtIpRemota.getText().trim().isEmpty() || txtPuertoRemoto.getText().trim().isEmpty()) {
                return false;
            }
        } else if (tipo == TipoConfig.OPERADOR) {
          
            if (txtPuesto.getText().trim().isEmpty()) {
            	
                return false;
            }
        }
        return true;
    }

    private void configurarCampos(JPanel panel) {
        if (tipo == TipoConfig.KIOSCO || tipo == TipoConfig.MONITOR) {
            panel.add(crearLabelEstilizado(" IP Destino (Remota):"));
            txtIpRemota = crearFieldEstilizado("127.0.0.1");
            panel.add(txtIpRemota); 

            panel.add(crearLabelEstilizado(" Puerto Destino:"));
            txtPuertoRemoto = crearFieldEstilizado("5000");
            aplicarFiltroRango(txtPuertoRemoto, 65535); 
            panel.add(txtPuertoRemoto);
        } 
        else if (tipo == TipoConfig.OPERADOR) {
             panel.add(crearLabelEstilizado(" Puesto: "));
             txtPuesto = crearFieldEstilizado("");
             aplicarFiltroRango(txtPuesto, 30); 
             panel.add(txtPuesto);
        } 
    }

    private void aplicarFiltroRango(JTextField textField, int maxPermitido) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String textoActual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String textoFuturo = textoActual.substring(0, offset) + text + textoActual.substring(offset + length);
                validarYPermitir(fb, offset, length, text, attrs, textoFuturo);
            }

            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                String textoActual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String textoFuturo = textoActual.substring(0, offset) + string + textoActual.substring(offset);
                validarYPermitir(fb, offset, 0, string, attr, textoFuturo);
            }

            private void validarYPermitir(FilterBypass fb, int offset, int length, String textoIngresado, AttributeSet attrs, String textoFuturo) throws BadLocationException {
                if (textoFuturo.isEmpty()) {
                    fb.replace(offset, length, textoIngresado, attrs);
                    return;
                }
                if (textoFuturo.matches("\\d+")) {
                    try {
                        long valor = Long.parseLong(textoFuturo);
                        if (valor <= maxPermitido) {
                            fb.replace(offset, length, textoIngresado, attrs);
                        }
                    } catch (NumberFormatException e) {
                        
                    }
                }
            }
        });
    }

    private JLabel crearLabelEstilizado(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONT_LABEL);
        label.setForeground(GRIS_TEXTO);
        return label;
    }

    private JTextField crearFieldEstilizado(String textoDefault) {
        JTextField field = new JTextField(textoDefault);
        field.setFont(FONT_CAMPO);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        
        Border bordeLinea = BorderFactory.createLineBorder(BORDE_CAMPO, 1);
        Border paddingInterno = new EmptyBorder(5, 8, 5, 8);
        field.setBorder(BorderFactory.createCompoundBorder(bordeLinea, paddingInterno));
        
        return field;
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusCycleRoot(true);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(new Color(255, 255, 255)); 
        
        boton.setBackground(new Color(0, 128, 0));
        
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(0, 50)); 
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(0, 100, 0)); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(VERDE_PRIMARIO); 
            }
        });

        return boton;
    }

    private String formatearTitulo(TipoConfig t) {
        String s = t.toString().replace("_", " ").toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String obtenerIpLocal() {
        try { 
            return InetAddress.getLocalHost().getHostAddress(); 
        } 
        catch (Exception e) { 
            return "127.0.0.1"; 
        }
    }


    public boolean fueConfirmado() { 
        return confirmado; 
    }
    
    public String getIpRemota() { 
        return txtIpRemota != null ? txtIpRemota.getText().trim() : null; 
    }
    
    public String getPuesto() { 
        return txtPuesto != null ? txtPuesto.getText().trim() : null; 
    }
  
    public int getPuertoRemoto() { 
        if(txtPuertoRemoto == null || txtPuertoRemoto.getText().trim().isEmpty()) 
            return 0;
        try {
            return Integer.parseInt(txtPuertoRemoto.getText().trim()); 
        }
        catch(NumberFormatException e) { 
            return 0; 
        }
    }
    
    public int getPuertoLocal() { 
        if(txtPuertoLocal == null || txtPuertoLocal.getText().trim().isEmpty()) 
            return 0;
        try {
            return Integer.parseInt(txtPuertoLocal.getText().trim()); 
        }
        catch(NumberFormatException e) { 
            return 0; 
        }
    }
}