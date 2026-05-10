package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GestorJson {
    private static final String FILE_PATH = "instancias.json";
    private static final long TOLERANCIA_MS = 20000; // 20 segundos

    // REGISTRO ATÓMICO: Evita colisiones y limpia zombis
    public static synchronized void registrarOActualizar(String ip, int puerto, boolean esPrincipal, boolean activo) {
        List<String> lineas = leerArchivo();
        List<String> nuevasLineas = new ArrayList<>();
        boolean encontrado = false;
        long ahora = System.currentTimeMillis();

        for (String l : lineas) {
            if (l.trim().isEmpty()) continue;

            try {
                String ipFila = extraerIP(l);
                int puertoFila = extraerPuerto(l);
                boolean esPrincipalFila = l.contains("\"esPrincipal\":true");
                boolean activoFila = l.contains("\"activo\":true");
                long timestampFila = extraerTimestamp(l);

                // 1. Limpieza automática de "Zombis" (si pasó el tiempo de tolerancia, lo matamos)
                if (activoFila && (ahora - timestampFila > TOLERANCIA_MS)) {
                    activoFila = false;
                    esPrincipalFila = false;
                }

                // 2. Si soy yo, renuevo mis datos y mi timestamp
                if (ipFila.equals(ip) && puertoFila == puerto) {
                    nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
                    encontrado = true;
                } else {
                    // 3. Destituir a otros "Principales" si yo me estoy declarando el nuevo líder
                    if (esPrincipal && esPrincipalFila) {
                        esPrincipalFila = false; 
                    }
                    // Agrego al otro servidor reconstruyendo su texto limpio
                    nuevasLineas.add(formatearJson(ipFila, puertoFila, esPrincipalFila, activoFila, timestampFila));
                }
            } catch (Exception e) {
                // Ignorar líneas corruptas del archivo
            }
        }

        // Si no estaba en el archivo, me agrego al final
        if (!encontrado) nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
        
        guardarArchivo(nuevasLineas);
    }

    public static synchronized String[] obtenerHeredero() {
        List<String> lineas = leerArchivo();
        List<String> candidatos = new ArrayList<>();

        for (String l : lineas) {
            if (l.contains("\"activo\":true") && !esRegistroViejo(l) && l.contains("\"esPrincipal\":false")) {
                candidatos.add(l);
            }
        }

        if (candidatos.isEmpty()) return null;

        // Gana el que tenga el puerto más bajo
        candidatos.sort((a, b) -> Integer.compare(extraerPuerto(a), extraerPuerto(b)));
        String ganador = candidatos.get(0);
        return new String[]{ extraerIP(ganador), String.valueOf(extraerPuerto(ganador)) };
    }

    public static synchronized String[] obtenerPrincipalActivo() {
        for (String l : leerArchivo()) {
            if (l.contains("\"esPrincipal\":true") && l.contains("\"activo\":true") && !esRegistroViejo(l)) {
                return new String[]{ extraerIP(l), String.valueOf(extraerPuerto(l)) };
            }
        }
        return null;
    }

    public static synchronized void marcarInactivo(String ip, int puerto) {
        List<String> lineas = leerArchivo();
        List<String> nuevasLineas = new ArrayList<>();
        for (String l : lineas) {
            if (l.trim().isEmpty()) continue;
            try {
                if (extraerIP(l).equals(ip) && extraerPuerto(l) == puerto) {
                    nuevasLineas.add(formatearJson(ip, puerto, false, false, System.currentTimeMillis()));
                } else { 
                    nuevasLineas.add(l); 
                }
            } catch (Exception e) { }
        }
        guardarArchivo(nuevasLineas);
    }

    // --- Helpers de extracción seguros ---
    private static int extraerPuerto(String l) { return Integer.parseInt(l.split("\"puerto\":")[1].split(",")[0].trim()); }
    private static String extraerIP(String l) { return l.split("\"ip\":\"")[1].split("\"")[0]; }
    private static long extraerTimestamp(String l) { return Long.parseLong(l.split("\"timestamp\":")[1].replace("}", "").trim()); }

    private static boolean esRegistroViejo(String linea) {
        try {
            long ts = extraerTimestamp(linea);
            return (System.currentTimeMillis() - ts) > TOLERANCIA_MS;
        } catch (Exception e) { return true; }
    }

    private static String formatearJson(String ip, int puerto, boolean esP, boolean act, long ts) {
        return "{\"ip\":\"" + ip + "\", \"puerto\":" + puerto + ", \"esPrincipal\":" + esP + ", \"activo\":" + act + ", \"timestamp\":" + ts + "}";
    }

    private static List<String> leerArchivo() {
        try { return Files.exists(Paths.get(FILE_PATH)) ? Files.readAllLines(Paths.get(FILE_PATH)) : new ArrayList<>(); }
        catch (IOException e) { return new ArrayList<>(); }
    }

    private static void guardarArchivo(List<String> lineas) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String l : lineas) if (!l.trim().isEmpty()) out.println(l.trim());
        } catch (IOException e) {}
    }
}