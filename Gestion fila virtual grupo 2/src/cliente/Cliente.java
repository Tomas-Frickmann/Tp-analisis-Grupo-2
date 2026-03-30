package cliente;

import java.io.Serializable;
import java.time.LocalTime;

public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dni;
    private LocalTime horaLlegada;

    public Cliente(String dni) {
        this.dni = dni;
        this.horaLlegada = LocalTime.now();
    }

    public String getDni() { return dni; }
    public LocalTime getHoraLlegada() { return horaLlegada; }
}