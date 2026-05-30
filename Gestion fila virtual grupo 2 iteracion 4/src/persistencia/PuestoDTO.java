package persistencia;

import java.io.Serializable;

public class PuestoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ip;
    private String puerto;
    private String dni;
    private int reintentos;
    private int nroPuesto;
    private boolean activo;

    public PuestoDTO(String ip, String puerto, String dni, int reintentos, int nroPuesto, boolean activo) {
        this.ip = ip;
        this.puerto = puerto;
        this.dni = dni;
        this.reintentos = reintentos;
        this.nroPuesto = nroPuesto;
        this.activo = activo;
    }

    public String getIp() { return ip; }
    public String getPuerto() { return puerto; }
    public String getDni() { return dni; }
    public int getReintentos() { return reintentos; }
    public int getNroPuesto() { return nroPuesto; }
    public boolean isActivo() { return activo; }
}