package servidor;

public class Puesto {
	private String ip;
	private String puerto;
	private String dni;
	private int reintentos;
	private int nroPuesto;
	private boolean activo;
	
	
	
	public Puesto(String ip, String puerto,String nroPuesto, boolean activo) {
		this.ip=ip;
		this.puerto=puerto;
		this.nroPuesto= Integer.parseInt(nroPuesto);
		this.activo=activo;
	
	
	}
	
	public Puesto(String ip, String puerto, String dni, int reintentos, int nroPuesto, boolean activo) {
		super();
		this.ip = ip;
		this.puerto = puerto;
		this.dni = dni;
		this.reintentos = reintentos;
		this.nroPuesto = nroPuesto;
		this.activo = activo;
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
		setReintentos(2);
	}
	public boolean isActivo() {
		return activo;
	}
	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	
}
