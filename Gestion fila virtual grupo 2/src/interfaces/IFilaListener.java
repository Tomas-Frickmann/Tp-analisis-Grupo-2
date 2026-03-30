package interfaces;

public interface IFilaListener {
    // Se dispara cuando llega un cliente nuevo
    void alCambiarFila(int nuevoTamano);
    
    // Se dispara cuando ocurre un error de red
    void alOcurrirError(String mensaje,String titulo);
}