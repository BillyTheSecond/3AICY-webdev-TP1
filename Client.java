import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class Client {


    public static void main(String[] args) {

        int port = 5001; // port du serveur

        try {
            // socket de communication pour envoyer des données au serveur
            Socket communication = new Socket("localhost", port);
            System.out.println("Connecté au serveur: " + communication.getInetAddress());
            
            // Flux de sortie pour envoyer des messages au serveur
            PrintWriter out = new PrintWriter(communication.getOutputStream(), true);
            
            // Scanner pour lire l'entrée utilisateur
            Scanner scanner = new Scanner(System.in);
            
            System.out.print("Entrez votre message: ");
            String message = scanner.nextLine();
            
            // Envoi du message au serveur
            out.println(message);
            System.out.println("Message envoyé: " + message);
            
            scanner.close();
            communication.close();
        } catch (UnknownHostException e) {
            e.printStackTrace(); // fonction de Exception qui dit que paso y adonde
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }
}