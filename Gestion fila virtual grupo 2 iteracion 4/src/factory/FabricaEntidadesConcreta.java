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
        // Este constructor (el de 4 parámetros) ya inicializa el ultimoContacto
        // por dentro con System.currentTimeMillis(), así que está perfecto así.
        return new Puesto(ip, puerto, nroPuesto, activo);
    }

    @Override
    // ACÁ AGREGAMOS EL "long ultimoContacto" AL FINAL
    public Puesto crearPuestoClonado(String ip, String puerto, String dni, int reintentos, int nroPuesto, boolean activo, long ultimoContacto) {
        return new Puesto(ip, puerto, dni, reintentos, nroPuesto, activo, ultimoContacto);
    }
}