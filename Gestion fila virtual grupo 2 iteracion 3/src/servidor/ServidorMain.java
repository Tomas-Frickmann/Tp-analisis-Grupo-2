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

        // El FileLock asegura que si lanzas varios servidores a la vez, 
        // entren al JSON de a uno por vez para decidir quién manda.
        try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) { 

            System.out.println("[SISTEMA] Iniciando secuencia de arranque...");

            // 1. ELECCIÓN DE PUERTO FÍSICO
            // Primero intentamos ocupar el puerto principal (5000)
            try (ServerSocket test = new ServerSocket(config.getPuertoPrincipal())) {
                miPuerto = config.getPuertoPrincipal();
                miIp = config.getIpPrincipal();
            } catch (IOException e) {
                // Si el 5000 está ocupado, buscamos el siguiente disponible para respaldos
                miPuerto = buscarPuertoLibre(config.getPuertoRespaldo());
                miIp = config.getIpRespaldo();
            }

            // 2. ELECCIÓN DE ROL LÓGICO
            // Revisamos el JSON. Si no hay nadie vivo mandando, tomamos el mando.
            String[] principalActual = GestorJson.obtenerPrincipalActivo();

            if (principalActual == null) {
                esRespaldo = false;
                System.out.println("[SISTEMA] >>> Rol: PRINCIPAL | Puerto: " + miPuerto);
            } else {
                esRespaldo = true;
                System.out.println("[SISTEMA] >>> Rol: RESPALDO | Puerto: " + miPuerto);
                System.out.println("[SISTEMA] >>> Vigila a: " + principalActual[0] + ":" + principalActual[1]);
            }

            // 3. REGISTRO INICIAL
            GestorJson.registrarOActualizar(miIp, miPuerto, !esRespaldo, true);

        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] No se pudo coordinar el inicio: " + e.getMessage());
            return;
        }

        // 4. HILO DE HEARTBEAT (Latido cada 10 segundos)
        Thread heartbeatThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000);
                    // Actualizamos el timestamp en el JSON para que no nos marquen como muertos
                    GestorJson.registrarOActualizar(miIp, miPuerto, !isEsRespaldo(), true);
                } catch (InterruptedException e) { 
                    break; 
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        // 5. SHUTDOWN HOOK (Cierre limpio)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SISTEMA] Cerrando servidor...");
            GestorJson.marcarInactivo(miIp, miPuerto); 
        }));

        // 6. INICIO DE LÓGICA
        ServidorLogic logica = new ServidorLogic(config, esRespaldo, miPuerto);
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