package persistencia;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XmlPantallaDAO implements PantallaDAO {
    @Override
    public void guardarPantalla(List<String> llamados, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_pantalla.xml"))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<llamados>");
            for (String llamado : llamados) {
                pw.println("  <llamado>" + llamado + "</llamado>");
            }
            pw.println("</llamados>");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public List<String> leerPantalla(String nombreArchivo) {
        List<String> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_pantalla.xml");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains("<llamado>")) {
                    String llamado = linea.replace("<llamado>", "").replace("</llamado>", "").trim();
                    lista.add(llamado);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}