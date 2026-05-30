package seguridad;

public class CifradoVigenere implements EstrategiaCifrado {

    @Override
    public String encriptar(String texto, String claveSecreta) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            
            int codigo = (texto.charAt(i) + claveSecreta.charAt(i % claveSecreta.length()));
            sb.append(codigo);
            if (i < texto.length() - 1) sb.append("-");
        }
        return sb.toString();
    }

    @Override
    public String desencriptar(String textoCifrado, String claveSecreta) {
        if (textoCifrado == null || textoCifrado.isEmpty() || !textoCifrado.contains("-")) {
            return textoCifrado;
        }
        StringBuilder sb = new StringBuilder();
        String[] numeros = textoCifrado.split("-");
        
        for (int i = 0; i < numeros.length; i++) {
            int codigoCifrado = Integer.parseInt(numeros[i]);
            
            char original = (char) (codigoCifrado - claveSecreta.charAt(i % claveSecreta.length()));
            sb.append(original);
        }
        return sb.toString();
    }
}