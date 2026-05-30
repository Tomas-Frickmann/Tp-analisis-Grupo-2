package persistencia;
import java.util.List;

import persistencia.PuestoDTO;

public interface PuestoDAO {

	void guardarPuestos(List<PuestoDTO> puestos, String nombreArchivo);
    List<PuestoDTO> leerPuestos(String nombreArchivo);
}
