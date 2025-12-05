import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;


public class Client {

    // Thread pour recevoir les messages du serveur en continu
    static class ReceptionThread extends Thread {
        private BufferedReader in;
        private volatile boolean running = true;
        
        public ReceptionThread(BufferedReader in) {
            this.in = in;
        }
        
        @Override
        public void run() {
            try {
                String message;
                while (running && (message = in.readLine()) != null) {
                    System.out.println("\n[Serveur] " + message);
                    System.out.print("Vous: ");
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Connexion au serveur perdue.");
                }
            }
        }
        
        public void stopThread() {
            running = false;
        }
    }
    
    // Thread pour envoyer les messages au serveur
    static class EnvoiThread extends Thread {
        private PrintWriter out;
        private Scanner scanner;
        private volatile boolean running = true;
        
        public EnvoiThread(PrintWriter out, Scanner scanner) {
            this.out = out;
            this.scanner = scanner;
        }
        
        @Override
        public void run() {
            try {
                // initialiser la connexion (premier message = pseudo de l'utilisateur)
                System.out.print("Entrez votre pseudo: ");
                String pseudo = scanner.nextLine();
                out.println(pseudo);
                
                while (running) {
                    System.out.print("Vous: ");
                    String message = scanner.nextLine();
                    
                    if (message.equals("/quit")) {
                        out.println(message);
                        running = false;
                        System.out.println("Déconnexion...");
                        break;
                    }
                    
                    out.println(message);
                }
            } catch (Exception e) {
                if (running) {
                    System.out.println("Erreur lors de l'envoi: " + e.getMessage());
                }
            }
        }
        
        public boolean isRunning() {
            return running;
        }
    }


    public static void main(String[] args) {

        int port = 5001; // port du serveur

        try {
            // socket de communication pour envoyer des données au serveur
            Socket communication = new Socket("localhost", port);
            System.out.println("Connecté au serveur: " + communication.getInetAddress());
            
            // Flux de sortie pour envoyer des messages au serveur
            PrintWriter out = new PrintWriter(communication.getOutputStream(), true);
            
            // Flux d'entrée pour recevoir des messages du serveur
            BufferedReader in = new BufferedReader(new InputStreamReader(communication.getInputStream()));
            
            // Scanner pour lire l'entrée utilisateur
            Scanner scanner = new Scanner(System.in);
            
            // Création et démarrage des threads
            ReceptionThread receptionThread = new ReceptionThread(in);
            EnvoiThread envoiThread = new EnvoiThread(out, scanner);
            
            receptionThread.start();
            envoiThread.start();
            
            // Attendre que le thread d'envoi se termine (quand l'utilisateur tape /quit)
            envoiThread.join();
            
            // Arrêter le thread de réception
            receptionThread.stopThread();
            
            // Fermeture des ressources
            scanner.close();
            communication.close();
            System.out.println("Déconnecté du serveur.");
            
        } catch (UnknownHostException e) {
            e.printStackTrace(); // fonction de Exception qui dit que paso y adonde
        } catch (IOException e) {
            e.printStackTrace(); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}