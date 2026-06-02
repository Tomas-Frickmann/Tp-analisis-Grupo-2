package factory;

import util.Cliente;
import util.Puesto;

public class FabricaEntidadesConcreta implements IFabricaEntidades {

    @Override
    public Cliente crearCliente(String dni) {
       
        return new Cliente(dni);
    }

    @Override
    public Puesto crearPuesto(String ip, String puerto, String nroPuesto, boolean activo) {
        return new Puesto(ip, puerto, nroPuesto, activo);
    }

    @Override
    public Puesto crearPuestoClonado(String ip, String puerto, String dni, int reintentos, int nroPuesto, boolean activo) {
        return new Puesto(ip, puerto, dni, reintentos, nroPuesto, activo);
    }
}