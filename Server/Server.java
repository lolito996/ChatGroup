package Server;
import java.io.*;
import java.net.*;

public class Server {

     public static void main(String[] args) {

        int PORT = 3500;
        Chatters clientes = new Chatters(); //lista de clientes

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                //clientes.addPerson();

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientes);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                //crea el objeto para gestionar al cliente y le envia la informacion necesaria
                //inicia el hilo para ese cliente
                

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
}

