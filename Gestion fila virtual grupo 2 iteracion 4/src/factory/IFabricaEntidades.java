package factory;

import util.Cliente;
import util.Puesto;

public interface IFabricaEntidades {
    // Para crear un cliente que entra a la fila
    Cliente crearCliente(String dni);
    
    // Para registrar un puesto nuevo (vacío)
    Puesto crearPuesto(String ip, String puerto, String nroPuesto, boolean activo);
    
    // Para clonar un puesto que viene del estado del servidor principal
    Puesto crearPuestoClonado(String ip, String puerto, String dni, int reintentos, int nroPuesto, boolean activo);
}