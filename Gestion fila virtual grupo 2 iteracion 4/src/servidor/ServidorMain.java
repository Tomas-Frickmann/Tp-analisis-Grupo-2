package servidor;

import util.ConfigServidor;
import util.GestorJson;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ServidorMain {

    private static String miIp;
    private static int miPuerto;
    private static volatile boolean esRespaldo;

    public static void main(String[] args) {
        ConfigServidor config = new ConfigServidor("config_servidores.properties");
        File lockFile = new File("eleccion_lider.lock");

        try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) { 

            System.out.println("[SISTEMA] Iniciando secuencia de arranque...");

            try (ServerSocket test = new ServerSocket(config.getPuertoPrincipal())) {
                miPuerto = config.getPuertoPrincipal();
                miIp = config.getIpPrincipal();
            } catch (IOException e) {
                miPuerto = buscarPuertoLibre(config.getPuertoRespaldo());
                miIp = config.getIpRespaldo();
            }

            String[] principalActual = GestorJson.obtenerPrincipalActivo();

            if (principalActual != null) {
                if (Integer.parseInt(principalActual[1]) == miPuerto) {
                    principalActual = null; 
                } else {
                    try (java.net.Socket socketPing = new java.net.Socket(principalActual[0], Integer.parseInt(principalActual[1]))) {
                        System.out.println("[SISTEMA] Conexión confirmada con el Principal verídico.");
                    } catch (IOException e) {
                        System.out.println("[SISTEMA] Registro fantasma detectado en JSON (" + principalActual[0] + ":" + principalActual[1] + "). Limpiando...");
                        GestorJson.marcarInactivo(principalActual[0], Integer.parseInt(principalActual[1]));
                        principalActual = null; 
                    }
                }
            }

            if (principalActual == null) {
                esRespaldo = false;
                System.out.println("[SISTEMA] >>> Rol: PRINCIPAL | Puerto: " + miPuerto);
            } else {
                esRespaldo = true;
                System.out.println("[SISTEMA] >>> Rol: RESPALDO | Puerto: " + miPuerto);
                System.out.println("[SISTEMA] >>> Vigila a: " + principalActual[0] + ":" + principalActual[1]);
            }

            GestorJson.registrarOActualizar(miIp, miPuerto, !esRespaldo, true);

        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] No se pudo coordinar el inicio: " + e.getMessage());
            return;
        }

        Thread heartbeatThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000);
                    GestorJson.registrarOActualizar(miIp, miPuerto, !isEsRespaldo(), true);
                } catch (InterruptedException e) { 
                    break; 
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        ServidorLogic logica = new ServidorLogic(config, esRespaldo, miPuerto);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SISTEMA] Cerrando servidor...");
            GestorJson.marcarInactivo(miIp, miPuerto); 
            if (!ServidorMain.isEsRespaldo()) {
                logica.guardarEstadoEnDisco(); 
           }
        }));

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
    
    public static boolean isEsRespaldo() { return esRespaldo; }
    public static void setEsRespaldo(boolean valor) { esRespaldo = valor; }
}