package persistencia; 
import util.*;

public class FabricaProductor {
    public static DAOFactory obtenerFabrica(String formato) {
        switch (formato.toUpperCase()) {
            case "TXT": return new TxtDAOFactory();
            case "XML": return new XmlDAOFactory();
            case "JSON": default: return new JsonDAOFactory();
        }
    }
}