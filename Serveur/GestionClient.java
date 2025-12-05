import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GestionClient implements Runnable {
    private Socket socket;  // Socket de communication pour CE client
    private Serveur serveur;
    private BufferedReader in;
    private PrintWriter out;
    private String pseudo;
    private boolean connected;

    public GestionClient(Socket socket, Serveur serveur) {
        this.socket = socket;  // Chaque Client a son propre socket
        this.serveur = serveur;
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            // flux d'entrée/sortie 
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            //  pseudo
            pseudo = in.readLine();
            if (pseudo == null || pseudo.trim().isEmpty()) {
                pseudo = "Anonyme_" + socket.getPort();
            }
            
            System.out.println(pseudo + " a rejoint le salon (socket: " + socket.getPort() + ")");
            serveur.broadcastToAll("*** " + pseudo + " a rejoint le salon ***");
            
            // Lire les messages
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }
                
                String formattedMessage = pseudo + ": " + message;
                System.out.println(formattedMessage);
                serveur.broadcastToAll(formattedMessage);
            }
            
        } catch (IOException e) {
            System.err.println("Erreur avec le client " + pseudo + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
        }
    }

    private void disconnect() {
        connected = false;
        
        if (pseudo != null) {
            System.out.println(pseudo + " s'est déconnecté");
            serveur.broadcastToAll("*** " + pseudo + " a quitté le salon ***");
        }
        
        serveur.removeClient(this);
        close();
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
