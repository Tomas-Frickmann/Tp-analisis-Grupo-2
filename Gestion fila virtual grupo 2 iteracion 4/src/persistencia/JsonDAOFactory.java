package persistencia;
import util.*;

public class JsonDAOFactory implements DAOFactory {
    @Override public ClienteDAO crearClienteDAO() { return new JsonClienteDAO(); }
    @Override public PuestoDAO crearPuestoDAO() { return new JsonPuestoDAO(); }
    @Override public PantallaDAO crearPantallaDAO() { return new JsonPantallaDAO(); }
}