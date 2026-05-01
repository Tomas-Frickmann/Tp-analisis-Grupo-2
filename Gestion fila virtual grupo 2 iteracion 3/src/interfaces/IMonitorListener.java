package interfaces;

import java.util.LinkedList;

public interface IMonitorListener {
	void alRecibirNuevoLlamado(LinkedList<String> dni);
    void alOcurrirError(String mensaje,String titulo);
}
