public class Main {
    public static void main(String[] args) {
        int port = 5001;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide, utilisation du port par défaut: 9876");
            }
        }
        
        Serveur serveur = new Serveur(port);
        Thread serveurThread = new Thread(serveur);
        serveurThread.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt du serveur...");
            serveur.stop();
        }));
    }
}
