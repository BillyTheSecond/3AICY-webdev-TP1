import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientGUI extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private TextArea messageArea;
    private TextField messageInput;
    private Button sendButton;
    private Button disconnectButton;
    private Label statusLabel;
    
    private ReceptionThread receptionThread;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Client");
        primaryStage.setWidth(600);
        primaryStage.setHeight(500);
        
        // Créer l'interface
        BorderPane root = new BorderPane();
        
        // Zone messages
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setPrefHeight(400);
        
        // Zone saisie
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        
        messageInput = new TextField();
        messageInput.setPromptText("Entrez votre message...");
        messageInput.setPrefWidth(400);
        
        sendButton = new Button("Envoyer");
        sendButton.setPrefWidth(80);
        sendButton.setOnAction(e -> sendMessage());
        
        // Envoyer message quand on appuie sur entrer
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                sendMessage();
            }
        });
        
        disconnectButton = new Button("Quitter");
        disconnectButton.setPrefWidth(80);
        disconnectButton.setOnAction(e -> {
            disconnect();
            System.exit(0);
        });
        
        bottomPanel.getChildren().addAll(messageInput, sendButton, disconnectButton);
        
        // Affichage status de connexion
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        statusLabel = new Label("Déconnecté");
        statusLabel.setStyle("-fx-text-fill: red;");
        topPanel.getChildren().add(statusLabel);
        
        // Assembler l'interface
        root.setTop(topPanel);
        root.setCenter(new ScrollPane(messageArea));
        root.setBottom(bottomPanel);
        
        // Afficher la fenêtre
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Connexion avec le psuedo
        showConnectionDialog(primaryStage);
        
        // Gérer la fermeture
        primaryStage.setOnCloseRequest(e -> {
            if (socket != null && !socket.isClosed()) {
                disconnect();
            }
        });
    }
    
    private void showConnectionDialog(Stage primaryStage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Connexion au serveur");
        dialog.setHeaderText("Entrez votre pseudo");
        
        TextField pseudoField = new TextField();
        pseudoField.setPromptText("Votre pseudo");
        
        dialog.getDialogPane().setContent(pseudoField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return pseudoField.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(pseudo -> {
            if (!pseudo.isEmpty()) {
                connectToServer(pseudo);
            } else {
                showAlert("Erreur", "Le pseudo ne peut pas être vide");
            }
        });
    }
    
    private void connectToServer(String pseudo) {
        try {
            socket = new Socket("localhost", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Envoyer le pseudo
            out.println(pseudo);
            
            // Mettre à jour le statut
            updateStatus("Connecté en tant que: " + pseudo, true);
            
            // Thread de réception
            receptionThread = new ReceptionThread(in);
            receptionThread.start();
            
        } catch (UnknownHostException e) {
            showAlert("Erreur", "Serveur non trouvé: " + e.getMessage());
            updateStatus("Erreur de connexion", false);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de se connecter: " + e.getMessage());
            updateStatus("Erreur de connexion", false);
        }
    }
    
    private void sendMessage() {
        String message = messageInput.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (out != null) {
            out.println(message);
            messageInput.clear();
            
            if (message.equalsIgnoreCase("/quit")) {
                disconnect();
            }
        }
    }
    
    private void disconnect() {
        try {
            if (out != null) {
                out.println("/quit");
            }
            
            if (receptionThread != null) {
                receptionThread.stopThread();
            }
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            updateStatus("Déconnecté", false);
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    private void updateStatus(String status, boolean connected) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            statusLabel.setStyle(connected ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            messageInput.setDisable(!connected);
            sendButton.setDisable(!connected);
        });
    }
    
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Thread pour recevoir les messages du serveur
    private class ReceptionThread extends Thread {
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
                    String finalMessage = message;
                    Platform.runLater(() -> {
                        messageArea.appendText(finalMessage + "\n");
                    });
                }
            } catch (IOException e) {
                if (running) {
                    Platform.runLater(() -> {
                        showAlert("Erreur", "Connexion au serveur perdue");
                        updateStatus("Déconnecté", false);
                    });
                }
            }
        }
        
        public void stopThread() {
            running = false;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}