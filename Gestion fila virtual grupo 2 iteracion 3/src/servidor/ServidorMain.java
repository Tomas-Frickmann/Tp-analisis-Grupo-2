package servidor;

import util.ConfigServidor;
import util.GestorJson;
import java.net.ServerSocket;
import java.io.IOException;

public class ServidorMain {

    private static String miIp;
    private static int miPuerto;
    private static volatile boolean esRespaldo;

    public static void main(String[] args) {
        ConfigServidor config = new ConfigServidor("config_servidores.properties");

        // 1. Determinar ROL por bloqueo de puerto
        try (ServerSocket socketTestigo = new ServerSocket(config.getPuertoPrincipal())) {
            esRespaldo = false;
            miIp = config.getIpPrincipal();
            miPuerto = config.getPuertoPrincipal();
            socketTestigo.close(); 
            System.out.println("[SISTEMA] >>> Iniciando como PRINCIPAL en puerto " + miPuerto);
        } catch (IOException e) {
            esRespaldo = true;
            miIp = config.getIpRespaldo();
            miPuerto = buscarPuertoLibre(config.getPuertoRespaldo());
            config.setPuertoRespaldo(miPuerto);
            System.out.println("[SISTEMA] >>> Iniciando como RESPALDO en puerto " + miPuerto);
            // Si agregaste el setter en ConfigServidor, usalo acá:
            config.setPuertoRespaldo(miPuerto); 
        }

        // 2. Registro inicial
        GestorJson.registrarOActualizar(miIp, miPuerto, !esRespaldo, true);

        // 3. Shutdown Hook (Cierre elegante)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GestorJson.marcarInactivo(miIp, miPuerto);
            System.out.println("[SISTEMA] Registro limpiado.");
        }));

        // 4. Hilo de Heartbeat (Mantiene vivo el Timestamp cada 10 seg)
        Thread heartbeatThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) { // <--- Importante
                try {
                    Thread.sleep(10000);
                    GestorJson.registrarOActualizar(miIp, miPuerto, !isEsRespaldo(), true);
                } catch (InterruptedException e) { 
                    System.out.println("[SISTEMA] Deteniendo Heartbeat...");
                    break; 
                }
            }
        });
        heartbeatThread.start();

        // 3. Shutdown Hook coordinado
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[CIERRE] Limpiando registros...");
            heartbeatThread.interrupt(); // 1. Frenamos el latido para que no vuelva a escribir activo:true
            GestorJson.marcarInactivo(miIp, miPuerto); // 2. Marcamos como inactivo
            System.out.println("[CIERRE] Servidor fuera de línea.");
        }));

        // 5. Iniciar Lógica Original
        ServidorLogic logica = new ServidorLogic(config, esRespaldo);
        logica.iniciarServidor();
    }

    private static int buscarPuertoLibre(int puertoBase) {
        int p = puertoBase;
        while (p < puertoBase + 50) {
            try (ServerSocket ss = new ServerSocket(p)) { return p; } 
            catch (IOException e) { p++; }
        }
        return p;
    }
    public static boolean isEsRespaldo() {
        return esRespaldo;
    }
    public static void setEsRespaldo(boolean valor) {
        esRespaldo = valor;
    }
}