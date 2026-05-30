package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ConfigPanelView extends JDialog {

    private static final long serialVersionUID = 1L;
    
    
    private JComboBox<String> cbAlgoritmo;
    private JTextField txtClave;
    
    
    private JComboBox<String> cbFormatoPersistencia;
    private JTextField txtNombreArchivo;

    private final String PATH_CONFIG = "config_servidores.properties";

    
    private final Color VERDE_PRIMARIO = new Color(46, 139, 87);   
    private final Color VERDE_FONDO = new Color(245, 255, 250);   
    private final Color GRIS_TEXTO = new Color(50, 50, 50);       
    private final Color BORDE_CAMPO = new Color(200, 220, 200);   
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_CAMPO = new Font("Consolas", Font.PLAIN, 13); 

    public ConfigPanelView() {
        setTitle("Panel de Control del Servidor - Configuración Avanzada");
        setModal(true);
        setResizable(false);

        
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15)); 
        panelPrincipal.setBackground(VERDE_FONDO);

        
        JLabel lblTitulo = new JLabel("Panel de Configuración del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(VERDE_PRIMARIO);
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Segoe UI", Font.BOLD, 12));

       
        pestanas.addTab("Seguridad ", crearPanelSeguridad());
        pestanas.addTab("Persistencia ",crearPanelPersistencia());
        
        panelPrincipal.add(pestanas, BorderLayout.CENTER);

        
        cargarValoresActuales();

        
        JButton btnGuardar = crearBotonEstilizado("Aplicar Configuración Global");
        btnGuardar.addActionListener(e -> {
            if (validarCampos()) {
                guardarValoresEnProperties();
                JOptionPane.showMessageDialog(this, 
                    "¡Configuración actualizada con éxito!\nLos cambios en seguridad e infraestructura impactarán de inmediato.", 
                    "Configuración Guardada", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        JPanel panelBoton = new JPanel(new BorderLayout());
        panelBoton.setBackground(VERDE_FONDO);
        panelBoton.setBorder(new EmptyBorder(10, 0, 0, 0));
        panelBoton.add(btnGuardar, BorderLayout.CENTER);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        getContentPane().add(panelPrincipal);
        pack();
        setSize(520, 360);
        setLocationRelativeTo(null);
    }

    
    private JPanel crearPanelSeguridad() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERDE_FONDO);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 20));
        form.setBackground(VERDE_FONDO);

        form.add(crearLabelEstilizado("Algoritmo de Cifrado:"));
        String[] algoritmos = {"Vigenere", "XOR"};
        cbAlgoritmo = new JComboBox<>(algoritmos);
        cbAlgoritmo.setFont(FONT_LABEL);
        form.add(cbAlgoritmo);

        form.add(crearLabelEstilizado("Clave Secreta Compartida:"));
        txtClave = crearFieldEstilizado("");
        form.add(txtClave);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

   
    private JPanel crearPanelPersistencia() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERDE_FONDO);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15)); 
        form.setBackground(VERDE_FONDO);

        form.add(crearLabelEstilizado("Formato de Almacenamiento:"));
        String[] formatos = {"JSON", "XML", "TXT"};
        cbFormatoPersistencia = new JComboBox<>(formatos);
        cbFormatoPersistencia.setFont(FONT_LABEL);
        form.add(cbFormatoPersistencia);

        form.add(crearLabelEstilizado("Nombre del Archivo (sin ext):"));
        txtNombreArchivo = crearFieldEstilizado("");
        form.add(txtNombreArchivo);

        boolean sistemaActivo = (GestorJson.obtenerPrincipalActivo() != null);

        if (sistemaActivo) {
            
            cbFormatoPersistencia.setEnabled(false);
            txtNombreArchivo.setEnabled(false);

            
            JLabel lblAlerta = new JLabel("<html><font color='red'><b>⚠️ Servidor Activo:</b> No se puede alterar la persistencia con el sistema en ejecución.</font></html>");
            lblAlerta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            form.add(new JLabel("")); 
            form.add(lblAlerta);
        }

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }
    private void cargarValoresActuales() {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(PATH_CONFIG)) {
            prop.load(in);
            
            cbAlgoritmo.setSelectedItem(prop.getProperty("seguridad.algoritmo", "Vigenere"));
            txtClave.setText(prop.getProperty("seguridad.clave", "ClaveSegura123"));
            
            
            cbFormatoPersistencia.setSelectedItem(prop.getProperty("persistencia.formato", "JSON"));
            txtNombreArchivo.setText(prop.getProperty("persistencia.archivo", "historial_filas"));
        } catch (IOException e) {
            
            cbAlgoritmo.setSelectedIndex(0);
            txtClave.setText("ClaveSegura123");
            cbFormatoPersistencia.setSelectedIndex(0);
            txtNombreArchivo.setText("historial_filas");
        }
    }

    private void guardarValoresEnProperties() {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(PATH_CONFIG)) {
            prop.load(in);
        } catch (IOException e) {
            
        }

        
        prop.setProperty("seguridad.algoritmo", cbAlgoritmo.getSelectedItem().toString());
        prop.setProperty("seguridad.clave", txtClave.getText().trim());

        
        prop.setProperty("persistencia.formato", cbFormatoPersistencia.getSelectedItem().toString());
        prop.setProperty("persistencia.archivo", txtNombreArchivo.getText().trim());

        try (FileOutputStream out = new FileOutputStream(PATH_CONFIG)) {
            prop.store(out, "Configuraciones globales actualizadas dinamicamente");
            System.out.println("[PANEL] Archivo .properties guardado correctamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al escribir en las propiedades.", "Error E/S", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarCampos() {
        if (txtClave.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La clave secreta de seguridad no puede estar vacía.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombreArchivo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe especificar un nombre de archivo válido para la persistencia.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
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
        Border bordeLinea = BorderFactory.createLineBorder(BORDE_CAMPO, 1);
        Border paddingInterno = new EmptyBorder(5, 8, 5, 8);
        field.setBorder(BorderFactory.createCompoundBorder(bordeLinea, paddingInterno));
        return field;
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE); 
        boton.setBackground(new Color(34, 139, 34));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(0, 45)); 
        return boton;
    }
}