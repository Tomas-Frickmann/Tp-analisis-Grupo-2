package persistencia;
import java.util.List;

public interface PantallaDAO {
	void guardarPantalla(List<String> llamados, String nombreArchivo);
    List<String> leerPantalla(String nombreArchivo);

}
