package persistencia;

import java.io.Serializable;

public class ClienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dni;

    public ClienteDTO(String dni) { 
        this.dni = dni; 
    }
    
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}