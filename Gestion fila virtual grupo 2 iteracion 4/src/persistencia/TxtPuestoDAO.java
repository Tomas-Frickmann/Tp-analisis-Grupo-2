package persistencia;
import java.io.*;
import java.util.*;
import persistencia.PuestoDTO;
import persistencia.PuestoDAO;

public class TxtPuestoDAO implements PuestoDAO {
    @Override
    public void guardarPuestos(List<PuestoDTO> puestos, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_puestos.txt"))) {
            for (PuestoDTO p : puestos) {
                pw.println(p.getIp() + ";" + p.getPuerto() + ";" + p.getDni() + ";" + 
                           p.getReintentos() + ";" + p.getNroPuesto() + ";" + p.isActivo());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<PuestoDTO> leerPuestos(String nombreArchivo) {
        List<PuestoDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_puestos.txt");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] d = l.split(";");
                lista.add(new PuestoDTO(d[0], d[1], d[2], Integer.parseInt(d[3]), Integer.parseInt(d[4]), Boolean.parseBoolean(d[5])));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}