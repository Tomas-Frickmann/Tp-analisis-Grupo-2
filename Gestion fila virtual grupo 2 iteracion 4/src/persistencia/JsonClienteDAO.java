package persistencia;
import java.io.*;
import java.util.*;
import persistencia.ClienteDTO;
import persistencia.ClienteDAO;

public class JsonClienteDAO implements ClienteDAO {
    @Override
    public void guardarCola(List<ClienteDTO> clientes, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_clientes.json"))) {
            pw.println("[");
            for (int i = 0; i < clientes.size(); i++) {
                pw.print("  { \"dni\": \"" + clientes.get(i).getDni() + "\" }");
                if (i < clientes.size() - 1) pw.println(","); else pw.println();
            }
            pw.println("]");
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<ClienteDTO> leerCola(String nombreArchivo) {
        List<ClienteDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_clientes.json");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.contains("\"dni\"")) {
                    lista.add(new ClienteDTO(l.split("\"dni\":")[1].split("\"")[1]));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}