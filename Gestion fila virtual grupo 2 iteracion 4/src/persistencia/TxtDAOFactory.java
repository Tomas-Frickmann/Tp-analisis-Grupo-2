package persistencia;
import util.*;

public class TxtDAOFactory implements DAOFactory {
    @Override public ClienteDAO crearClienteDAO() { return new TxtClienteDAO(); }
    @Override public PuestoDAO crearPuestoDAO() { return new TxtPuestoDAO(); }
    @Override public PantallaDAO crearPantallaDAO() { return new TxtPantallaDAO(); }
}