package persistencia;
import persistencia.ClienteDAO;
import persistencia.PantallaDAO;
import persistencia.PuestoDAO;
public class XmlDAOFactory implements DAOFactory {
    @Override public ClienteDAO crearClienteDAO() { return new XmlClienteDAO(); }
    @Override public PuestoDAO crearPuestoDAO() { return new XmlPuestoDAO(); }
    @Override public PantallaDAO crearPantallaDAO() { return new XmlPantallaDAO(); }
}