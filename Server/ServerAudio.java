package Server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerAudio {
    private static final int PORT = 12345;
    private static Map<String, ObjectOutputStream> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                username = (String) inputStream.readObject();
                clients.put(username, outputStream);

                while (true) {
                    Object receivedObj = inputStream.readObject();
                    if (receivedObj instanceof VoiceNote) {
                        VoiceNote voiceNote = (VoiceNote) receivedObj;
                        if (voiceNote.getSender().equals(username)) {
                            // Sending voice note to all clients except sender
                            for (ObjectOutputStream clientStream : clients.values()) {
                                if (clientStream != outputStream) {
                                    clientStream.writeObject(voiceNote);
                                }
                            }
                        } else {
                            // Sending voice note to specific client
                            ObjectOutputStream recipientStream = clients.get(voiceNote.getSender());
                            if (recipientStream != null) {
                                recipientStream.writeObject(voiceNote);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(username);
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}