package persistencia;
import java.io.*;
import java.util.*;
import persistencia.ClienteDTO;
import persistencia.ClienteDAO;

public class TxtClienteDAO implements ClienteDAO {
    @Override
    public void guardarCola(List<ClienteDTO> clientes, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_clientes.txt"))) {
            for (ClienteDTO c : clientes) { pw.println(c.getDni()); }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<ClienteDTO> leerCola(String nombreArchivo) {
        List<ClienteDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_clientes.txt");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) { lista.add(new ClienteDTO(l)); }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}