package servidor;

import cliente.Cliente;

public class Puesto {
	private String ip;
	private String puerto;
	private String dni;
	private int reintentos;
	private int nroPuesto;
	private static int nroPuestoSig = 1;
	
	
	public Puesto(String ip, String puerto) {
		this.ip=ip;
		this.puerto=puerto;
		this.nroPuesto=nroPuestoSig;
		nroPuestoSig++;
	}
	public String getIp() {
		return ip;
	}
	public String getPuerto() {
		return puerto;
	}
	public String getDni() {
		return dni;
	}
	public int getReintentos() {
		return reintentos;
	}
	public int getNroPuesto() {
		return nroPuesto;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setPuerto(String puerto) {
		this.puerto = puerto;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
	public void setReintentos(int reintentos) {
		this.reintentos = reintentos;
	}
	public void disminuirReintento() {
		this.reintentos=this.reintentos -1;
	}
	public void asignarClienteAlPuesto(Cliente c) {
		setDni(c.getDni());
		setReintentos(3);
	}
}
