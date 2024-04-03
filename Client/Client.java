package Client;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3500;
    private static final String AUDIO_FOLDER = "audios";

    public static void main(String[] args) throws InterruptedException{
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

            response = in.readLine();
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

            String option = "";
            do{
                option = userInput.readLine();
                out.println(option);
                switch(option){
                    case "1":
                        String groupName;
                        while ((groupName = userInput.readLine()) != null){
                            out.println(groupName);
                            out.flush();
                            break;
                        }
                        break;
                    case "2":
                    String enteringGroup;
                        while ((enteringGroup = userInput.readLine()) != null){
                            out.println(enteringGroup);
                            out.flush();
                            break;
                        }
                        break;
                    case "3":
                        String userInputMessage;
                        while ((userInputMessage = userInput.readLine()) != null){
                            out.println(userInputMessage);
                            out.flush();
                            break;
                        }
                        break;
                    case "0":
                        Thread.sleep(200);
                        break;
                    default:
                }

            }while(!option.equals("0"));

            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void print(Object o){System.out.println(o);}

}