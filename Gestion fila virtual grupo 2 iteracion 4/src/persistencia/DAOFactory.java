package persistencia;
import util.*;
public interface DAOFactory {
	ClienteDAO crearClienteDAO();
    PuestoDAO crearPuestoDAO();
    PantallaDAO crearPantallaDAO();

}
