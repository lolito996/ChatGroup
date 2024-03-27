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
            //canal de entrada para el usuario
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        
            //usando el socket, crear los canales de entrada in y salida out
                      
            //solicitar al usuario un alias, o nombre y enviarlo al servidor
            //no debe salir de este bloque hasta que el nombre no sea aceptado
            //al ser aceptado notificar, de lo contrario seguir pidiendo un alias
            
            System.out.print("Type username the user : ");
            String username = userInput.readLine();
            out.println(username);
            
            String response = in.readLine();
            System.out.println(response);

                 
            //creamos el objeto Lector e iniciamos el hilo que nos permitira estar atentos a los mensajes
            //que llegan del servidor
            //inicar el hilo
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


            //estar atento a la entrada del usuario para poner los mensajes en el canal de salida out
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