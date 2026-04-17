package monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


import interfaces.IVentana;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class MonitorVentana extends JFrame implements IVentana {

    private static final long serialVersionUID = 1L;
    private JLabel lblActual;
    private DefaultListModel<String> modeloHistorial = new DefaultListModel<>();

    private JPanel pnlActual;
    private JPanel mainPanel;
    private JPanel pnlHistorial;
    private JLabel titHist;
    private JList<String> list;
    
    private javax.swing.Timer blinkTimer;
    private int contadorParpadeo = 0;
    
    public MonitorVentana() {
        setTitle("PANTALLA DE TURNOS");
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        

        Color verdeFuerte = new Color(46, 125, 50);
        Color oscuroFondo = new Color(38, 50, 56);
        Color verdeClaro = new Color(200, 230, 201);

        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);


        pnlActual = new JPanel(new GridBagLayout());
        pnlActual.setBackground(oscuroFondo);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel tit = new JLabel("TURNO ACTUAL", SwingConstants.CENTER);
        tit.setFont(new Font("Arial", Font.BOLD, 50));
        tit.setForeground(verdeClaro);
        gbc.gridy = 0;
        pnlActual.add(tit, gbc);

        lblActual = new JLabel("---", SwingConstants.CENTER);

        lblActual.setFont(new Font("Arial", Font.BOLD, 120)); 
        lblActual.setForeground(Color.WHITE);
        gbc.gridy = 1;
        pnlActual.add(lblActual, gbc);


        pnlHistorial = new JPanel(new BorderLayout());
        pnlHistorial.setPreferredSize(new Dimension(450, 0));
        pnlHistorial.setBackground(new Color(232, 245, 233));
        pnlHistorial.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, verdeFuerte));

        titHist = new JLabel(" ÚLTIMOS LLAMADOS", SwingConstants.LEFT);
        titHist.setOpaque(true);
        titHist.setBackground(verdeFuerte);
        titHist.setForeground(Color.WHITE);
        titHist.setFont(new Font("Arial", Font.BOLD, 25));
        titHist.setPreferredSize(new Dimension(0, 60));

        list = new JList<>(modeloHistorial);
        list.setFont(new Font("Arial", Font.BOLD, 35));
        list.setBackground(new Color(232, 245, 233));
        list.setForeground(new Color(55, 71, 79));
        list.setFixedCellHeight(80);

        pnlHistorial.add(titHist, BorderLayout.NORTH);
        pnlHistorial.add(list, BorderLayout.CENTER);


        mainPanel.add(pnlActual, BorderLayout.CENTER);
        mainPanel.add(pnlHistorial, BorderLayout.EAST);
        
        setVisible(false); 
    }

    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    public void actualizarMonitor(LinkedList<String> historial) {
        if (historial == null || historial.isEmpty()) return;
        
        SwingUtilities.invokeLater(() -> {
            String actual = historial.getFirst();
            String textoGigante = "<html><div style='text-align: center;'>" + actual.replace(" - ", "<br>") + "</div></html>";
            lblActual.setText(textoGigante);  
            
            iniciarParpadeo();
            reproducirSonido();
            
            modeloHistorial.clear();
            Iterator<String> it = historial.iterator();
            
            if (it.hasNext()) {
                it.next(); 
            }

            while (it.hasNext()) {
                String s = it.next();
                this.modeloHistorial.addElement("  " + s);
            }
            
            this.repaint();
        });
    }
    private void iniciarParpadeo() {
        // Si ya estaba parpadeando por un llamado anterior, lo reseteamos
        if (blinkTimer != null && blinkTimer.isRunning()) {
            blinkTimer.stop();
        }

        contadorParpadeo = 0;
        Color colorOriginal = Color.WHITE;
        Color colorFondo = new Color(38, 50, 56); // El color oscuroFondo que definiste en el constructor

        // Creamos el timer: cada 500ms cambia de color
        blinkTimer = new javax.swing.Timer(250, e -> {
            if (contadorParpadeo < 10) { // Parpadeará 5 veces (encendido/apagado)
                if (lblActual.getForeground().equals(colorOriginal)) {
                    lblActual.setForeground(colorFondo);
                } else {
                    lblActual.setForeground(colorOriginal);
                }
                contadorParpadeo++;
            } else {
                // Al final lo dejamos siempre visible y frenamos el timer
                lblActual.setForeground(colorOriginal);
                blinkTimer.stop();
            }
        });
        
        blinkTimer.start();
    }
    private void reproducirSonido() {
        try {
            // Cargamos el archivo desde el classpath (carpeta src o resources)
            InputStream is = getClass().getResourceAsStream("campana.wav");
            // Necesitamos un BufferedInputStream para que sea compatible con mark/reset
            InputStream bufferedIn = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // Reproduce el sonido
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }
}