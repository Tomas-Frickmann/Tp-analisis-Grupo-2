package persistencia;
import java.io.*;
import java.util.*;
import persistencia.PantallaDAO;

public class JsonPantallaDAO implements PantallaDAO {
    @Override
    public void guardarPantalla(List<String> llamados, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_pantalla.json"))) {
            pw.println("[");
            for (int i = 0; i < llamados.size(); i++) {
                pw.print("  \"" + llamados.get(i) + "\"");
                if (i < llamados.size() - 1) pw.println(","); else pw.println();
            }
            pw.println("]");
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<String> leerPantalla(String nombreArchivo) {
        List<String> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_pantalla.json");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.contains("\"") && !l.contains("[")) lista.add(l.split("\"")[1]);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}