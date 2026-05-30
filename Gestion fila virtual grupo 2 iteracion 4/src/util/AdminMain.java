package util;

public class AdminMain {
    public static void main(String[] args) {
        
        System.out.println("[ADMIN] Abriendo Panel de Control de Infraestructura...");
        ConfigPanelView panel = new ConfigPanelView();
        panel.setVisible(true);
        System.exit(0);
    }
}