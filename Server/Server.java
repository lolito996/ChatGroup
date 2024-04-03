package Server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

     public static void main(String[] args) {

        int PORT = 3500;
        Chatters clientes = new Chatters(); //lista de clientes
        ArrayList<Group> groups = new ArrayList<>();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                //clientes.addPerson();

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientes, groups);
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

