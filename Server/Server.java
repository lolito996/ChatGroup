package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final int SIZE_POOL = 50;
    public static final int PORT = 3500;
    public static final int BUFFER_SIZE = 1024 + 4;

    public static void main(String[] args) {
        Chatters clientes = new Chatters(); //lista de clientes
        ArrayList<Group> groups = new ArrayList<>();
        ExecutorService threadPool = Executors.newFixedThreadPool(SIZE_POOL);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                threadPool.execute(new ClientHandler(clientSocket, clientes, groups));
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}


