package persistencia;
import java.io.*;
import java.util.*;

public class JsonPuestoDAO implements PuestoDAO {
    @Override
    public void guardarPuestos(List<PuestoDTO> puestos, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_puestos.json"))) {
            pw.println("[");
            for (int i = 0; i < puestos.size(); i++) {
                PuestoDTO p = puestos.get(i);
                pw.print("  { \"ip\":\"" + p.getIp() + "\", \"puerto\":\"" + p.getPuerto() + "\", \"dni\":\"" + p.getDni() + "\", \"reintentos\":" + p.getReintentos() + ", \"nroPuesto\":" + p.getNroPuesto() + ", \"activo\":" + p.isActivo() + ", \"ultimoContacto\":" + p.getUltimoContacto() + " }");
                if (i < puestos.size() - 1) pw.println(","); else pw.println();
            }
            pw.println("]");
        } catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public List<PuestoDTO> leerPuestos(String nombreArchivo) {
        List<PuestoDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_puestos.json");
        if (!file.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.contains("{")) {
                    String[] d = l.split(",");
                    String ip = d[0].split(":")[1].replace("\"", "").trim();
                    String puerto = d[1].split(":")[1].replace("\"", "").trim();
                    String dni = d[2].split(":")[1].replace("\"", "").trim();
                    int reint = Integer.parseInt(d[3].split(":")[1].trim());
                    int nro = Integer.parseInt(d[4].split(":")[1].trim());
                    // Usamos replace("}", "") por si lee un archivo viejo que no tenía ultimoContacto
                    boolean act = Boolean.parseBoolean(d[5].split(":")[1].replace("}", "").trim());
                    long ult = (d.length > 6) ? Long.parseLong(d[6].split(":")[1].replace("}", "").trim()) : System.currentTimeMillis();
                    
                    lista.add(new PuestoDTO(ip, puerto, dni, reint, nro, act, ult));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}