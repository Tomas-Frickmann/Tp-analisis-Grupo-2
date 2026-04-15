package servidor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import cliente.Cliente;

public class ServidorMain {

	private LinkedList<Puesto> listaPuestosRegistrados= new LinkedList<>();
	private Queue<Cliente> colaClientesEnEspera = new LinkedList<>();
	
	
	public void anadirCliente(String dni) {
		Cliente nuevo = new Cliente(dni);
		colaClientesEnEspera.add(nuevo);
	}
	public void anadirPuesto(String ip, String puerto) {
		Puesto nuevo= new Puesto(ip,puerto);
		listaPuestosRegistrados.add(nuevo);
	}
	
	public void validaCliente(String dni) {
		Iterator <Cliente> it=colaClientesEnEspera.iterator();
		Cliente aux;
		boolean existe=false;
		while (it.hasNext()){
			aux =it.next();
			if (aux.getDni().equals(dni)){
				existe=true;
				break;
			}	
		}
		if (existe){
			//devolver que ya existe el DNI en la cola
		}
		else {
			anadirCliente(dni);
		}
		
	}
	public void ValidaPuesto(String ip, String puerto) {
		Iterator <Puesto> it=listaPuestosRegistrados.iterator();
		Puesto aux;
		boolean existe=false;
		while (it.hasNext()){
			aux =it.next();
			if ((aux.getIp().equals(ip)) && (aux.getPuerto().equals(puerto))){
				existe=true;
				break;
			}	
		}
		if (existe){
			//devolver que ya existe la ip+puerto en la fila
		}
		else {
			anadirPuesto(ip,puerto);
		}
		
	}
	public void reintento(int nroPuesto) {
		Puesto p = listaPuestosRegistrados.get(nroPuesto - 1) ;
		if (p.getReintentos()>0) {
			p.disminuirReintento();
		}
		else {
			llamarSiguienteCliente(nroPuesto);// ASUMO QUE SI SE REINTENTA CUANDO LA CANTIDAD RESTANTE ES 0 SE LLAMA AL SIGUIENTE CIENTE AUTOMATIAMENTE
											  // SINO SE PUEDE MANDAR UN CARTEL QIUE DIGA SIN REINTENTOS RESTANTES
		}
	}
	public void llamarSiguienteCliente(int nroPuesto) {
		Puesto p = listaPuestosRegistrados.get(nroPuesto - 1);
		Cliente sig= colaClientesEnEspera.poll();
		if(sig != null) {
			p.asignarClienteAlPuesto(sig);
		}
		else {
			// ENVIAR MENSAJE DE QUE NO HAY CLIENES EN ESPERA
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
