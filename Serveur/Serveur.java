import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class Serveur implements Runnable {
    private ServerSocket serverSocket;
    private int port;
    private List<GestionClient> clients;
    private boolean running;
    
    public Serveur(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.running = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Serveur démarré sur le port " + port);
            System.out.println("En attente de connexions...");
            
            while (running) {
                try {
                    // Accepter une nouvelle connexion et créer un socket de communication pour ce client
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nouvelle connexion de: " + clientSocket.getInetAddress());
                    
                    // Créer un handler qui encapsule le socket de communication du client
                    GestionClient gestionClient = new GestionClient(clientSocket, this);
                    
                    // Sauvegarder dans la liste des clients
                    clients.add(gestionClient);
                    
                    // Démarrer un thread pour ce client
                    Thread clientThread = new Thread(gestionClient);
                    clientThread.start();
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Erreur lors de l'acceptation d'un client: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur du serveur: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    public synchronized void broadcastToAll(String message) {
        synchronized(clients) {
            for (GestionClient client : clients) {
                if (client.isConnected()) {
                    client.sendToClient(message);
                }
            }
        }
    }
    
    public synchronized void removeClient(GestionClient client) {
        synchronized(clients) {
            clients.remove(client);
        }
        System.out.println("Client retiré. Nombre de clients connectés: " + clients.size());
    }
    
    public void stop() {
        running = false;
        
        for (GestionClient client : clients) {
            client.close();
        }
        clients.clear();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Serveur arrêté");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur: " + e.getMessage());
        }
    }
}