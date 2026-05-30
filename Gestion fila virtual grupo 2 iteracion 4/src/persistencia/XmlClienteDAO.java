package persistencia;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import persistencia.ClienteDAO;
import persistencia.ClienteDAO;

public class XmlClienteDAO implements ClienteDAO {

    @Override
    public void guardarCola(List<ClienteDTO> clientes, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_clientes.xml"))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<clientes>");
            for (ClienteDTO c : clientes) {
                pw.println("  <cliente>");
                pw.println("    <dni>" + c.getDni() + "</dni>");
                pw.println("  </cliente>");
            }
            pw.println("</clientes>");
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    @Override
    public List<ClienteDTO> leerCola(String nombreArchivo) {
        List<ClienteDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_clientes.xml");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains("<dni>")) {
                    String dni = linea.replace("<dni>", "").replace("</dni>", "").trim();
                    lista.add(new ClienteDTO(dni));
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return lista;
    }
}