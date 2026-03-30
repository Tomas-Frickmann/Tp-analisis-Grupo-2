package cliente;
import javax.swing.*;
import java.awt.*;

public class ClienteView extends JFrame {
    private static final long serialVersionUID = 1L;
    public JTextField txtDni = new JTextField();
    public JButton btnConfirmar = new JButton("OBTENER TURNO");
    public JPanel pnlTeclado = new JPanel(new GridLayout(4, 3, 10, 10));

    public ClienteView() {
        setTitle("Terminal de Registro");
        setSize(400, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(245, 250, 248));

        txtDni.setFont(new Font("Arial", Font.BOLD, 35));
        txtDni.setHorizontalAlignment(JTextField.CENTER);
        txtDni.setEditable(false);
        txtDni.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 210), 2));

        btnConfirmar.setBackground(new Color(76, 175, 80));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 20));
        btnConfirmar.setPreferredSize(new Dimension(0, 70));

        String[] teclas = {"1","2","3","4","5","6","7","8","9","CLR","0","<-"};
        for(String t : teclas) {
            JButton b = new JButton(t);
            b.setFont(new Font("Arial", Font.BOLD, 22));
            b.setBackground(new Color(220, 235, 225));
            pnlTeclado.add(b);
        }

        add(txtDni, BorderLayout.NORTH);
        add(pnlTeclado, BorderLayout.CENTER);
        add(btnConfirmar, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }
}