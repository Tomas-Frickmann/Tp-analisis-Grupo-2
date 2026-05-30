package persistencia;
import java.io.*;
import java.util.*;
import persistencia.PantallaDAO;

public class TxtPantallaDAO implements PantallaDAO {
    @Override
    public void guardarPantalla(List<String> llamados, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_pantalla.txt"))) {
            for (String s : llamados) { pw.println(s); }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<String> leerPantalla(String nombreArchivo) {
        List<String> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_pantalla.txt");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) { lista.add(l); }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}