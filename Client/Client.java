package Client;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3500;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            System.out.println("connection established with the server");

            String message;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print("Type username the user : ");
            String username = userInput.readLine();
            out.println(username);
            
            String response = in.readLine();
            System.out.println(response);

            Thread readerThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readerThread.start();
            String userInputMessage;

            while ((userInputMessage = userInput.readLine()) != null) {
                out.println(userInputMessage);
                out.flush();
            }

            //socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}