package main;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.Scanner;
import main.java.com.checkers.api.GameServer;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(".env")) {
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            return;
        }

        // Get PORT from .env, default to 9000 if not found
        int port = Integer.parseInt(props.getProperty("PORT", "9000"));
        String host = props.getProperty("HOST", "localhost");
        String keystorePassword = props.getProperty("KEYSTORE_PASSWORD");
        String keystorePath = props.getProperty("KEYSTORE_PATH", "keystore.jks");
        GameServer gameServer = new GameServer(host, port, keystorePath, keystorePassword);

        // Create a thread for handling user input
        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                String status = "running";
                while (!status.equals("quit")) {
                    System.out.println("Enter 'quit' to stop the server:");
                    status = scanner.nextLine();
                }
            }
            gameServer.stopServer(); // Stop the server when 'quit' is entered
            System.exit(0); // Ensure the program exits
        });

        // Start the input thread
        inputThread.start();

        // Main server logic
        try {
            gameServer.startServer();
        } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | CertificateException
                | KeyStoreException | IOException e) {
            
            e.printStackTrace();
        }
        System.out.println("Server is running...");
    }
}