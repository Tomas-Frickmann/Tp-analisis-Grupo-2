package cliente;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteModel {
    public boolean enviarDni(String dni) {
        try (Socket s = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            out.println(dni);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}