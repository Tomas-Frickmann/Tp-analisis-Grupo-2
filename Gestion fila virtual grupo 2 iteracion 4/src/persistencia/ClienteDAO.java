package persistencia;
import java.util.List;

import persistencia.ClienteDTO;

public interface ClienteDAO {
	void guardarCola(List<ClienteDTO> clientes, String nombreArchivo);
    List<ClienteDTO> leerCola(String nombreArchivo);

}
