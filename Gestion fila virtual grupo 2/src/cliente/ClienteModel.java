package cliente;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteModel {
    private String IP;
    private Socket socket=null;
    private PrintWriter out;
    private int PORT;

    public ClienteModel(String ipDestino, int puertoDestino) {
        super();
        this.IP = ipDestino;
        this.PORT = puertoDestino;
    }

    public boolean enviarTurnoPorSocket(String dni) {
        try {
            
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                this.socket = new Socket(this.IP, this.PORT);
                this.out = new PrintWriter(socket.getOutputStream(), true);
            }

            
            out.println(dni);

            
            if (out.checkError()) {
               
                desconectar(); 
                return false;
            }

            return true; 
            
        } catch (Exception e) {
            e.printStackTrace();
            desconectar();
            return false;
        }
    }
   
  

    public void desconectar() {
        try {
            if (out != null) 
            	out.close();
            if (socket != null) 
            	socket.close();
        } catch (Exception e) {
            
        } finally {
           
            this.socket = null;
            this.out = null;
        }
    }


}