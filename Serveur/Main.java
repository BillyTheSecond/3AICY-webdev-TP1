public class Main {
    public static void main(String[] args) {
        int port = 5001;
        
        
        Serveur serveur = new Serveur(port);
        Thread serveurThread = new Thread(serveur);
        serveurThread.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt du serveur...");
            serveur.stop();
        }));
    }
}
