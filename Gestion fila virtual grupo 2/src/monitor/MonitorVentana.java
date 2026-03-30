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

public class MonitorVentana extends JFrame {

	
	
    private static final long serialVersionUID = 1L;
    private JLabel lblActual;
    private DefaultListModel<String> modeloHistorial = new DefaultListModel<>();

    private JPanel pnlActual;
    private JPanel mainPanel;
    private JPanel pnlHistorial;
    private JLabel titHist;
    private JList<String> list;
    
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
        lblActual.setFont(new Font("Arial", Font.BOLD, 250));
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

        
        setVisible(true);
    }

    
    

    public void mostrarMensaje(String mensaje,String titulo,int tipo) {
 	   
 	   JOptionPane.showMessageDialog(this,mensaje,titulo,tipo);
    }


	
	
	public void actualizarMonitor(LinkedList<String> historial) {
		if (historial == null || historial.isEmpty()) return;
	    SwingUtilities.invokeLater(() -> {
	       String actual=historial.getFirst();
	        lblActual.setText(actual);	   
	        modeloHistorial.clear();
	        Iterator<String> it = historial.iterator();
	        
	    
	        if (it.hasNext()) {
	            it.next(); 
	        }

	        
	        while (it.hasNext()) {
	            String s = it.next();
	            this.modeloHistorial.addElement("  DNI: " + s);
	        }

	        
	        this.repaint();
	    });
	}

}

