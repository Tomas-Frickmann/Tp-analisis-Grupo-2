package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class GestorJson {
    private static final String FILE_PATH = "instancias.json";
    private static final long TOLERANCIA_MS = 20000;

    public static void registrarOActualizar(String ip, int puerto, boolean esPrincipal, boolean activo) {
        long ahora = System.currentTimeMillis();
        
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {

            List<String> lineas = new ArrayList<>();
            String linea;
            raf.seek(0);
            
            while ((linea = raf.readLine()) != null) { 
                lineas.add(linea); 
            }

            List<String> nuevasLineas = new ArrayList<>();
            boolean encontrado = false;

            for (String l : lineas) {
                // Filtro anti-basura: Ignorar líneas vacías o mal formadas
                if (l == null || l.trim().isEmpty() || !l.contains("{")) {
                    continue; 
                }
                
                try {
                    String ipFila = extraerIP(l);
                    int puertoFila = extraerPuerto(l);
                    boolean esPrincipalFila = l.contains("\"esPrincipal\":true");
                    boolean activoFila = l.contains("\"activo\":true");
                    long timestampFila = extraerTimestamp(l);

                    if (activoFila && (ahora - timestampFila > TOLERANCIA_MS)) {
                        activoFila = false; 
                        esPrincipalFila = false;
                    }

                    if (ipFila.equals(ip) && puertoFila == puerto) {
                        nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
                        encontrado = true;
                    } else {
                        if (esPrincipal && esPrincipalFila) {
                            esPrincipalFila = false;
                        }
                        nuevasLineas.add(formatearJson(ipFila, puertoFila, esPrincipalFila, activoFila, timestampFila));
                    }
                } catch (Exception e) {
                    // Si falla el parseo de una línea en particular, la ignoramos y seguimos
                    continue;
                }
            }
            
            if (!encontrado) {
                nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
            }

            // Vaciamos el archivo
            raf.setLength(0);
            
            // ESCRIBIMOS DIRECTO CON EL RAF (Evita el ClosedChannelException)
            for (String nl : nuevasLineas) {
                raf.writeBytes(nl + "\n");
            }
            
        } catch (IOException e) { 
            System.err.println("Error JSON Atómico: " + e.getMessage()); 
        }
    }
    public static void marcarInactivo(String ip, int puerto) {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {
            
            List<String> lineas = new ArrayList<>();
            String l;
            raf.seek(0);
            
            while ((l = raf.readLine()) != null) { 
                lineas.add(l); 
            }

            // Vaciamos el archivo
            raf.setLength(0);
            
            // ESCRIBIMOS DIRECTO CON EL RAF
            for (String linea : lineas) {
                if (linea == null || linea.trim().isEmpty()) continue; // Ignoramos basura

                if (linea.contains("\"ip\":\"" + ip + "\"") && linea.contains("\"puerto\":" + puerto)) {
                    raf.writeBytes(formatearJson(ip, puerto, false, false, System.currentTimeMillis()) + "\n");
                } else { 
                    raf.writeBytes(linea + "\n"); 
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al marcar inactivo: " + e.getMessage());
        }
    }
    public static synchronized List<String[]> obtenerRespaldosActivos() {
        List<String[]> respaldos = new ArrayList<>();
        
        for (String l : leerArchivo()) {
            if (l.contains("\"esPrincipal\":false") && l.contains("\"activo\":true") && !esRegistroViejo(l)) {
                respaldos.add(new String[]{ extraerIP(l), String.valueOf(extraerPuerto(l)) });
            }
        }
        
        return respaldos;
    }

    public static synchronized String[] obtenerPrincipalActivo() {
        for (String l : leerArchivo()) {
            if (l.contains("\"esPrincipal\":true") && l.contains("\"activo\":true") && !esRegistroViejo(l)) {
                return new String[]{ extraerIP(l), String.valueOf(extraerPuerto(l)) };
            }
        }
        return null;
    }

    public static synchronized String[] obtenerHeredero() {
        List<String> candidatos = new ArrayList<>();
        
        for (String l : leerArchivo()) {
            if (l.contains("\"activo\":true") && !esRegistroViejo(l) && l.contains("\"esPrincipal\":false")) {
                candidatos.add(l);
            }
        }
        
        if (candidatos.isEmpty()) {
            return null;
        }
        
        candidatos.sort((a, b) -> Integer.compare(extraerPuerto(a), extraerPuerto(b)));
        String ganador = candidatos.get(0);
        return new String[]{ extraerIP(ganador), String.valueOf(extraerPuerto(ganador)) };
    }

    // --- Helpers de extracción expandidos para mayor legibilidad ---
    
    private static int extraerPuerto(String l) { 
        return Integer.parseInt(l.split("\"puerto\":")[1].split(",")[0].trim()); 
    }
    
    private static String extraerIP(String l) { 
        return l.split("\"ip\":\"")[1].split("\"")[0]; 
    }
    
    private static long extraerTimestamp(String l) { 
        return Long.parseLong(l.split("\"timestamp\":")[1].replace("}", "").trim()); 
    }
    
    private static boolean esRegistroViejo(String l) { 
        long timestamp = extraerTimestamp(l);
        return (System.currentTimeMillis() - timestamp) > TOLERANCIA_MS; 
    }
    
    private static String formatearJson(String ip, int puerto, boolean esPrincipal, boolean activo, long timestamp) {
        return "{\"ip\":\"" + ip + "\", \"puerto\":" + puerto + ", \"esPrincipal\":" + esPrincipal + ", \"activo\":" + activo + ", \"timestamp\":" + timestamp + "}";
    }
    
    private static List<String> leerArchivo() {
        try { 
            if (Files.exists(Paths.get(FILE_PATH))) {
                return Files.readAllLines(Paths.get(FILE_PATH)); 
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) { 
            return new ArrayList<>(); 
        }
    }
}