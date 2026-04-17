package cliente;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import interfaces.IVentana;

public class ClienteView extends JFrame implements IVentana  {   
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private JTextField txtDni;
private ActionListener actionlistener;
private String operadorIP = "localhost"; 
private JPanel mainPanel, topPanel, keypadPanel, botPanel;
private JLabel lblTitulo;
private JButton btnRegistrar;
private String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "CLR", "0", "<-"};

private ArrayList<JButton> keypadButtons = new ArrayList<>();

public ClienteView() {
	
    setTitle("SISTEMA DE FILA VIRTUAL - REGISTRO DE TURNO");
    setSize(480, 700); 
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false); 


    Color verdeFondo = new Color(245, 250, 248); 
    Color verdeBoton = new Color(76, 175, 80);    
    Color verdeBotonHover = new Color(102, 187, 106);
    Color verdeBotonKeypad = new Color(178, 223, 219); 
    Color grisTexto = new Color(66, 66, 66);

   
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(20, 20));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    mainPanel.setBackground(verdeFondo);
    setContentPane(mainPanel);

   
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setOpaque(false);

    lblTitulo = new JLabel("POR FAVOR, INGRESE SU DNI:", SwingConstants.CENTER);
    lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
    lblTitulo.setForeground(grisTexto);
    lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
    topPanel.add(lblTitulo);
    topPanel.add(Box.createVerticalStrut(20)); 

    txtDni = new JTextField();
    txtDni.setFont(new Font("Arial", Font.BOLD, 36));
    txtDni.setHorizontalAlignment(JTextField.CENTER);
    txtDni.setForeground(grisTexto);
    txtDni.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(grisTexto, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
   
    topPanel.add(txtDni);
    topPanel.add(Box.createVerticalStrut(30)); 

    mainPanel.add(topPanel, BorderLayout.NORTH);


    keypadPanel = new JPanel();
    keypadPanel.setLayout(new GridLayout(4, 3, 10, 10)); 
    keypadPanel.setOpaque(false);
    this.txtDni.setEditable(false);


    

    for (String key : keys) {
        JButton btnKey = new JButton(key);
        
        btnKey.setFont(new Font("Arial", Font.BOLD, 28));
        btnKey.setFocusable(false); 
        btnKey.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        if (key.equals("CLR") || key.equals("<-")) {
            btnKey.setBackground(Color.LIGHT_GRAY);
            btnKey.setFont(new Font("Arial", Font.BOLD, 20));
            btnKey.setForeground(Color.BLACK);
        } else {
            btnKey.setBackground(verdeBotonKeypad);
            btnKey.setForeground(grisTexto);
        }
        
        
        keypadPanel.add(btnKey);
        this.keypadButtons.add(btnKey);
        
    }

    mainPanel.add(keypadPanel, BorderLayout.CENTER);


   
    botPanel = new JPanel();
    botPanel.setOpaque(false);
    botPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

    btnRegistrar = new JButton("OBTENER MI TURNO");
    btnRegistrar.setPreferredSize(new Dimension(350, 70));
    btnRegistrar.setFont(new Font("Arial", Font.BOLD, 22));
    btnRegistrar.setFocusable(false);
    btnRegistrar.setBackground(verdeBoton);
    btnRegistrar.setForeground(Color.WHITE);
    btnRegistrar.setBorder(BorderFactory.createRaisedBevelBorder());

    
    
    
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

    setLocationRelativeTo(null); 
    setVisible(true);
}

public void setActionListener(ActionListener actionListener) {
	this.actionlistener = actionListener;
	btnRegistrar.addActionListener(actionListener);
	for (JButton button : keypadButtons) {
		button.addActionListener(actionListener);
	}
}



public void setTextDni(String string) {
	// TODO Auto-generated method stub
	this.txtDni.setText(string);
}

public String getTextDni() {
	// TODO Auto-generated method stub
	return this.txtDni.getText();
}

@Override
public void mostrarMensaje(String mensaje, String titulo, int tipo) {
	// TODO Auto-generated method stub
	JOptionPane.showMessageDialog(this,mensaje,titulo,tipo);
}

}