package persistencia;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XmlPuestoDAO implements PuestoDAO {
    @Override
    public void guardarPuestos(List<PuestoDTO> puestos, String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo + "_puestos.xml"))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<puestos>");
            for (PuestoDTO p : puestos) {
                pw.println("  <puesto>");
                pw.println("    <ip>" + p.getIp() + "</ip>");
                pw.println("    <puerto>" + p.getPuerto() + "</puerto>");
                pw.println("    <dni>" + p.getDni() + "</dni>");
                pw.println("    <reintentos>" + p.getReintentos() + "</reintentos>");
                pw.println("    <nroPuesto>" + p.getNroPuesto() + "</nroPuesto>");
                pw.println("    <activo>" + p.isActivo() + "</activo>");
                pw.println("    <ultimoContacto>" + p.getUltimoContacto() + "</ultimoContacto>");
                pw.println("  </puesto>");
            }
            pw.println("</puestos>");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public List<PuestoDTO> leerPuestos(String nombreArchivo) {
        List<PuestoDTO> lista = new ArrayList<>();
        File file = new File(nombreArchivo + "_puestos.xml");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            String ip = "", puerto = "", dni = "";
            int reintentos = 0, nroPuesto = 0;
            boolean activo = false;
            long ultimoContacto = System.currentTimeMillis();

            while ((linea = br.readLine()) != null) {
                if (linea.contains("<ip>")) ip = linea.replace("<ip>", "").replace("</ip>", "").trim();
                else if (linea.contains("<puerto>")) puerto = linea.replace("<puerto>", "").replace("</puerto>", "").trim();
                else if (linea.contains("<dni>")) dni = linea.replace("<dni>", "").replace("</dni>", "").trim();
                else if (linea.contains("<reintentos>")) reintentos = Integer.parseInt(linea.replace("<reintentos>", "").replace("</reintentos>", "").trim());
                else if (linea.contains("<nroPuesto>")) nroPuesto = Integer.parseInt(linea.replace("<nroPuesto>", "").replace("</nroPuesto>", "").trim());
                else if (linea.contains("<activo>")) activo = Boolean.parseBoolean(linea.replace("<activo>", "").replace("</activo>", "").trim());
                else if (linea.contains("<ultimoContacto>")) ultimoContacto = Long.parseLong(linea.replace("<ultimoContacto>", "").replace("</ultimoContacto>", "").trim());
                else if (linea.contains("</puesto>")) {
                    lista.add(new PuestoDTO(ip, puerto, dni, reintentos, nroPuesto, activo, ultimoContacto));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}