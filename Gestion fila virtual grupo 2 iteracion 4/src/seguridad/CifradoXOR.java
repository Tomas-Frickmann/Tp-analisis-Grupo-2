package seguridad;

public class CifradoXOR implements EstrategiaCifrado {

    @Override
    public String encriptar(String texto, String claveSecreta) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            
            int cifrado = texto.charAt(i) ^ claveSecreta.charAt(i % claveSecreta.length());
            sb.append(cifrado);
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
            int valorCifrado = Integer.parseInt(numeros[i]);
            
            char original = (char) (valorCifrado ^ claveSecreta.charAt(i % claveSecreta.length()));
            sb.append(original);
        }
        return sb.toString();
    }
}