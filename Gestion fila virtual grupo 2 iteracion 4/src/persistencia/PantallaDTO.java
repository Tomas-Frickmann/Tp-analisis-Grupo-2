package persistencia;

import java.io.Serializable;

public class PantallaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dniCifrado;
    private String nroPuesto;

    public PantallaDTO(String dniCifrado, String nroPuesto) {
        this.dniCifrado = dniCifrado;
        this.nroPuesto = nroPuesto;
    }

    public String getDniCifrado() { return dniCifrado; }
    public String getNroPuesto() { return nroPuesto; }
}