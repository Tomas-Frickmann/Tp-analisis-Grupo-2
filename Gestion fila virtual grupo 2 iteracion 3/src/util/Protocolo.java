package util;

public class Protocolo {
    private Protocolo() {}


    public static final String CMD_REGISTRO = "REGISTRO";
    public static final String CMD_LLAMAR = "LLAMAR";
    public static final String CMD_RELLAMAR = "RE_LLAMAR";
    public static final String CMD_NUEVO_CLIENTE = "NUEVO_CLIENTE";
    public static final String CMD_INFO_FILA = "INFO_FILA";
    public static final String CMD_REGISTRO_MONITOR = "REGISTRO_MONITOR";
    public static final String OK_REGISTRADO = "OK:REGISTRADO";
    public static final String OK_CLIENTE_CREADO = "OK:CLIENTE_REGISTRADO";
    public static final String OK_RELLAMADO = "OK:RE_LLAMADO";
    public static final String MSG_ACTUALIZAR_MONITOR = "ACTUALIZAR_MONITOR";
    public static final String ERR_FILA_VACIA = "ERROR:FILA_VACIA";
    public static final String ERR_DNI_DUPLICADO = "ERROR:DNI_DUPLICADO";
    public static final String ERR_PUESTO_EXISTE = "ERROR:PUESTO_YA_EXISTE";
    public static final String ERR_PUESTO_NO_EXISTE = "ERROR:PUESTO_NO_EXISTE";
    public static final String ERR_COMANDO = "ERROR:COMANDO_INVALIDO";
    public static final String ERR_CONEXION = "ERROR:CONEXION";
    public static final String SIN_REINTENTOS = "ERROR:CLIENTE_SIN_REINTENTOS";

    public static final String SEPARADOR = ";";
	
}