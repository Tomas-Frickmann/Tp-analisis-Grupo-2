package operador;
 

 public class OperadorMain {

	private static OperadorControlador controlador;
	
	public static void main(String[] args) {
		controlador = new OperadorControlador();
		OperadorVentana ventana = new OperadorVentana();
		controlador.setVentana(ventana);
		

	}

}
