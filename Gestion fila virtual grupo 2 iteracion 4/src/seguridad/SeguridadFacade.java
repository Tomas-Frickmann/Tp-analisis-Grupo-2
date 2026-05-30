package seguridad;

import util.ConfigServidor;

public class SeguridadFacade {

    
    private static final EstrategiaCifrado xorInstance = new CifradoXOR();
    private static final EstrategiaCifrado vigenereInstance = new CifradoVigenere();

    
    private static EstrategiaCifrado obtenerEstrategia(String algoritmo) {
        if ("VIGENERE".equalsIgnoreCase(algoritmo)) {
            return vigenereInstance;
        }
        return xorInstance; 
    }

    public static String cifrarDni(String dni) {
        
        ConfigServidor config = new ConfigServidor("config_servidores.properties");
        String algoritmo = config.getAlgoritmoCifrado(); 
        String clave = config.getClaveSecreta();        

        EstrategiaCifrado estrategia = obtenerEstrategia(algoritmo);
        
     // 👉 PRINT DE PRUEBA: Muestra qué entró y qué salió
        String resultado = estrategia.encriptar(dni, clave);
        System.out.println("[SEGURIDAD - CIFRANDO] Original: " + dni + " -> Red: " + resultado + " (Usando: " + algoritmo + ")");
        return estrategia.encriptar(dni, clave);
    }

    public static String descifrarDni(String dniCifrado) {
        ConfigServidor config = new ConfigServidor("config_servidores.properties");
        String algoritmo = config.getAlgoritmoCifrado();
        String clave = config.getClaveSecreta();

        
        
        EstrategiaCifrado estrategia = obtenerEstrategia(algoritmo);
        
     // 👉 PRINT DE PRUEBA: Muestra cómo se recupera el dato
       String resultado = estrategia.desencriptar(dniCifrado, clave);
        System.out.println("[SEGURIDAD - DESCIFRANDO] Red: " + dniCifrado + " -> Recuperado: " + resultado + " (Usando: " + algoritmo + ")");
        
        return estrategia.desencriptar(dniCifrado, clave);
    }
}