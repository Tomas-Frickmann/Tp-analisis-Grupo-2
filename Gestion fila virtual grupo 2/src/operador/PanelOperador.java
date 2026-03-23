package operador;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class PanelOperador extends JFrame {
    private static final long serialVersionUID = 1L;
    private Queue<String> fila = new LinkedList<>();
    private JLabel lblContador;
    private JLabel lblEstado;
    private String monitorIP = "localhost";

    public PanelOperador() {
        setTitle("PUESTO DE ATENCIÓN - CONTROL DE FILA");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Colores gama verde
        Color verdeFondo = new Color(240, 248, 240);
        Color verdeBoton = new Color(67, 160, 71);
        Color grisTexto = new Color(55, 71, 79);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(verdeFondo);
        setContentPane(mainPanel);

        // Indicador de Fila
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
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
        JButton btnSiguiente = new JButton("LLAMAR SIGUIENTE");
        btnSiguiente.setFont(new Font("Arial", Font.BOLD, 20));
        btnSiguiente.setBackground(verdeBoton);
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.setFocusable(false);
        btnSiguiente.setBorder(BorderFactory.createRaisedBevelBorder());

        btnSiguiente.addActionListener(e -> llamarSiguiente());
        mainPanel.add(btnSiguiente, BorderLayout.CENTER);

        new Thread(this::escucharClientes).start();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void escucharClientes() {
        try (ServerSocket server = new ServerSocket(5000)) {
            while (true) {
                Socket s = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String dni = in.readLine();
                if (dni != null) {
                    fila.add(dni);
                    actualizarUI();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void llamarSiguiente() {
        if (!fila.isEmpty()) {
            String dni = fila.poll();
            notificarMonitor(dni);
            actualizarUI();
        } else {
            JOptionPane.showMessageDialog(this, "No hay clientes en espera.", "FILA VACÍA", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void notificarMonitor(String dni) {
        try (Socket s = new Socket(monitorIP, 6000);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            out.println(dni);
        } catch (Exception ex) { 
            lblEstado.setText("ERROR: MONITOR NO ENCONTRADO");
            lblEstado.setForeground(Color.RED);
        }
    }

    private void actualizarUI() {
        SwingUtilities.invokeLater(() -> {
            lblContador.setText("Clientes en espera: " + fila.size());
            lblEstado.setText("SISTEMA ACTIVO");
            lblEstado.setForeground(new Color(67, 160, 71));
        });
    }

    public static void main(String[] args) { new PanelOperador(); }
}