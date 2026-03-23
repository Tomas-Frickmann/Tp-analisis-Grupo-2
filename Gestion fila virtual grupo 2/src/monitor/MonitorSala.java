package monitor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class MonitorSala extends JFrame {
    private static final long serialVersionUID = 1L;
    private JLabel lblActual;
    private DefaultListModel<String> modeloHistorial = new DefaultListModel<>();

    public MonitorSala() {
        setTitle("PANTALLA DE TURNOS");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Color verdeFuerte = new Color(46, 125, 50);
        Color oscuroFondo = new Color(38, 50, 56);
        Color verdeClaro = new Color(200, 230, 201);

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // Panel Izquierdo: Turno Actual
        JPanel pnlActual = new JPanel(new GridBagLayout());
        pnlActual.setBackground(oscuroFondo);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel tit = new JLabel("TURNO ACTUAL", SwingConstants.CENTER);
        tit.setFont(new Font("Arial", Font.BOLD, 50));
        tit.setForeground(verdeClaro);
        gbc.gridy = 0;
        pnlActual.add(tit, gbc);

        lblActual = new JLabel("---", SwingConstants.CENTER);
        lblActual.setFont(new Font("Arial", Font.BOLD, 250));
        lblActual.setForeground(Color.WHITE);
        gbc.gridy = 1;
        pnlActual.add(lblActual, gbc);

        // Panel Derecho: Historial
        JPanel pnlHistorial = new JPanel(new BorderLayout());
        pnlHistorial.setPreferredSize(new Dimension(450, 0));
        pnlHistorial.setBackground(new Color(232, 245, 233));
        pnlHistorial.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, verdeFuerte));

        JLabel titHist = new JLabel(" ÚLTIMOS LLAMADOS", SwingConstants.LEFT);
        titHist.setOpaque(true);
        titHist.setBackground(verdeFuerte);
        titHist.setForeground(Color.WHITE);
        titHist.setFont(new Font("Arial", Font.BOLD, 25));
        titHist.setPreferredSize(new Dimension(0, 60));

        JList<String> list = new JList<>(modeloHistorial);
        list.setFont(new Font("Arial", Font.BOLD, 35));
        list.setBackground(new Color(232, 245, 233));
        list.setForeground(new Color(55, 71, 79));
        list.setFixedCellHeight(80);

        pnlHistorial.add(titHist, BorderLayout.NORTH);
        pnlHistorial.add(list, BorderLayout.CENTER);

        mainPanel.add(pnlActual, BorderLayout.CENTER);
        mainPanel.add(pnlHistorial, BorderLayout.EAST);

        new Thread(this::escucharLlamados).start();
        setVisible(true);
    }

    private void escucharLlamados() {
        try (ServerSocket server = new ServerSocket(6000)) {
            while (true) {
                Socket s = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String dni = in.readLine();
                if (dni != null) {
                    actualizarPantalla(dni);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void actualizarPantalla(String dni) {
        SwingUtilities.invokeLater(() -> {
            if (!lblActual.getText().equals("---")) {
                modeloHistorial.add(0, "  DNI: " + lblActual.getText());
                if (modeloHistorial.size() > 4) modeloHistorial.remove(4);
            }
            lblActual.setText(dni);
            Toolkit.getDefaultToolkit().beep();
        });
    }

    public static void main(String[] args) { new MonitorSala(); }
}