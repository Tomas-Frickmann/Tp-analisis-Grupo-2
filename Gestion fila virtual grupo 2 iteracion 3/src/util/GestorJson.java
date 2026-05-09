package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GestorJson {
    private static final String FILE_PATH = "instancias.json";
    private static final long TOLERANCIA_MS = 20000; // 20 segundos para considerar a alguien "muerto"

    // REGISTRO ATÓMICO: Evita que dos se anoten como principal a la vez
    public static synchronized void registrarOActualizar(String ip, int puerto, boolean esPrincipal, boolean activo) {
        List<String> lineas = leerArchivo();
        List<String> nuevasLineas = new ArrayList<>();
        boolean encontrado = false;
        long ahora = System.currentTimeMillis();

        for (String linea : lineas) {
            String l = linea;
            // Si yo soy principal, cualquier otro que diga ser principal lo paso a false
            if (esPrincipal && l.contains("\"esPrincipal\":true") && !l.contains("\"puerto\":" + puerto)) {
                l = l.replace("\"esPrincipal\":true", "\"esPrincipal\":false");
            }

            if (l.contains("\"ip\":\"" + ip + "\"") && l.contains("\"puerto\":" + puerto)) {
                nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
                encontrado = true;
            } else {
                nuevasLineas.add(l);
            }
        }
        if (!encontrado) nuevasLineas.add(formatearJson(ip, puerto, esPrincipal, activo, ahora));
        guardarArchivo(nuevasLineas);
    }

    // EL ÁRBITRO: Decide quién es el heredero legal por puerto menor
    public static synchronized String[] obtenerHeredero() {
        List<String> lineas = leerArchivo();
        List<String> candidatos = new ArrayList<>();

        for (String l : lineas) {
            if (l.contains("\"activo\":true") && !esRegistroViejo(l) && l.contains("\"esPrincipal\":false")) {
                candidatos.add(l);
            }
        }

        if (candidatos.isEmpty()) return null;

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
            if (l.contains("\"ip\":\"" + ip + "\"") && l.contains("\"puerto\":" + puerto)) {
                nuevasLineas.add(formatearJson(ip, puerto, false, false, System.currentTimeMillis()));
            } else { nuevasLineas.add(l); }
        }
        guardarArchivo(nuevasLineas);
    }

    // Helpers de extracción
    private static int extraerPuerto(String l) { return Integer.parseInt(l.split("\"puerto\":")[1].split(",")[0].trim()); }
    private static String extraerIP(String l) { return l.split("\"ip\":\"")[1].split("\"")[0]; }

    private static boolean esRegistroViejo(String linea) {
        try {
            long ts = Long.parseLong(linea.split("\"timestamp\":")[1].replace("}", "").trim());
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