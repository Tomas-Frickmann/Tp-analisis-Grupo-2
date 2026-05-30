package seguridad;

public interface EstrategiaCifrado {
    String encriptar(String texto, String claveSecreta);
    String desencriptar(String textoCifrado, String claveSecreta);
}