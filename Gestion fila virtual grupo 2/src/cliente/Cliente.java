package cliente;
import java.time.LocalTime;

public class Cliente  {
    private String dni;
    private LocalTime horaLlegada;

    public Cliente(String dni) {
        this.dni = dni;
        this.horaLlegada = LocalTime.now();
    }

    public String getDni() { return dni; }
    public LocalTime getHoraLlegada() { return horaLlegada; }
}