package cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;

public class TerminalCliente extends JFrame {
    private static final long serialVersionUID = 1L; // Corrección para el aviso de Eclipse
    private JTextField txtDni;
    private String operadorIP = "localhost"; // Cambiar por la IP real del Operador en la LAN

    public TerminalCliente() {
        setTitle("SISTEMA DE FILA VIRTUAL - REGISTRO DE TURNO");
        setSize(480, 700); // Tamaño más alto para el teclado
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false); // Diseño fijo

        // Paleta de Colores Suaves (Gama Verde)
        Color verdeFondo = new Color(245, 250, 248); // Mint Cream muy suave
        Color verdeBoton = new Color(76, 175, 80);    // Verde Material suave
        Color verdeBotonHover = new Color(102, 187, 106);
        Color verdeBotonKeypad = new Color(178, 223, 219); // Salvia pálido
        Color grisTexto = new Color(66, 66, 66);

        // Panel Principal con Padding y Fondo Suave
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(verdeFondo);
        setContentPane(mainPanel);

        // --- Área Superior: Título y Entrada ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel lblTitulo = new JLabel("POR FAVOR, INGRESE SU DNI:", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(grisTexto);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(lblTitulo);
        topPanel.add(Box.createVerticalStrut(20)); // Espaciado vertical

        txtDni = new JTextField();
        txtDni.setFont(new Font("Arial", Font.BOLD, 36));
        txtDni.setHorizontalAlignment(JTextField.CENTER);
        txtDni.setForeground(grisTexto);
        txtDni.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(grisTexto, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        // Desactivar el teclado físico si se desea solo táctil (opcional)
        // txtDni.setEditable(false);
        topPanel.add(txtDni);
        topPanel.add(Box.createVerticalStrut(30)); // Espaciado vertical

        mainPanel.add(topPanel, BorderLayout.NORTH);


        // --- Área Central: Teclado Numérico ---
        JPanel keypadPanel = new JPanel();
        keypadPanel.setLayout(new GridLayout(4, 3, 10, 10)); // 4 filas, 3 columnas, gap de 10px
        keypadPanel.setOpaque(false);

        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "CLR", "0", "<-"};

        KeypadListener keypadListener = new KeypadListener();

        for (String key : keys) {
            JButton btnKey = new JButton(key);
            btnKey.setFont(new Font("Arial", Font.BOLD, 28));
            btnKey.setFocusable(false); // Evitar que el foco robe el cursor
            btnKey.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

            if (key.equals("CLR") || key.equals("<-")) {
                btnKey.setBackground(Color.LIGHT_GRAY);
                btnKey.setFont(new Font("Arial", Font.BOLD, 20));
                btnKey.setForeground(Color.BLACK);
            } else {
                btnKey.setBackground(verdeBotonKeypad);
                btnKey.setForeground(grisTexto);
            }
            
            btnKey.addActionListener(keypadListener);
            keypadPanel.add(btnKey);
        }

        mainPanel.add(keypadPanel, BorderLayout.CENTER);


        // --- Área Inferior: Botón Confirmar ---
        JPanel botPanel = new JPanel();
        botPanel.setOpaque(false);
        botPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton btnRegistrar = new JButton("OBTENER MI TURNO");
        btnRegistrar.setPreferredSize(new Dimension(350, 70));
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 22));
        btnRegistrar.setFocusable(false);
        btnRegistrar.setBackground(verdeBoton);
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setBorder(BorderFactory.createRaisedBevelBorder());

        // Lógica de Sockets (inalterada, solo cambiada de método para claridad)
        btnRegistrar.addActionListener(e -> enviarTurnoPorSocket());
        
        // Efecto visual simple (esto no se ve en Swing puro sin librerías, pero es buena práctica)
        btnRegistrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnRegistrar.setBackground(verdeBotonHover);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnRegistrar.setBackground(verdeBoton);
            }
        });

        botPanel.add(btnRegistrar);
        mainPanel.add(botPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // Centrar en pantalla
        setVisible(true);
    }

    /**
     * Lógica para el Teclado Numérico
     */
    private class KeypadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String currentText = txtDni.getText();
            String key = e.getActionCommand();

            if (key.equals("CLR")) {
                txtDni.setText(""); // Borrado completo
            } else if (key.equals("<-")) {
                if (!currentText.isEmpty()) {
                    txtDni.setText(currentText.substring(0, currentText.length() - 1)); // Borrar último
                }
            } else {
                // Validación básica de longitud (DNI argentino máx 9 dígitos, por ejemplo)
                if (currentText.length() < 9) {
                    txtDni.setText(currentText + key); // Añadir dígito
                }
            }
        }
    }

    /**
     * Lógica de Comunicación por Sockets
     */
    private void enviarTurnoPorSocket() {
        String dni = txtDni.getText().trim();
        // Validación: No vacío, solo números, longitud coherente
        if (!dni.isEmpty() && dni.matches("\\d{6,9}")) {
            try (Socket s = new Socket(operadorIP, 5000);
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                out.println(dni);
                
                // Diálogo agradable
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 18));
                JOptionPane.showMessageDialog(this, "Registro Exitoso. \nSu turno ha sido añadido.", "TURNO CONFIRMADO", JOptionPane.INFORMATION_MESSAGE);
                txtDni.setText(""); // Limpiar para el siguiente
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error de Red: No se pudo conectar con el Operador.", "ERROR DE CONEXIÓN", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "El DNI ingresado es inválido.\nPor favor, verifique.", "DNI INVÁLIDO", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Ejecutar en el hilo de Swing
        SwingUtilities.invokeLater(() -> new TerminalCliente());
    }
}