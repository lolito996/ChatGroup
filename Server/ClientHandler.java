package Server;
import java.io.*;
import java.net.*;


class ClientHandler implements Runnable {

    private Socket clientSocket; // Socket para la conexión con el cliente
    private BufferedReader in; // Flujo de entrada para leer los mensajes del cliente
    private PrintWriter out; // Flujo de salida para enviar mensajes al cliente
    private String clientName; // Nombre de usuario del cliente
    Chatters clientes; // Objeto que contiene la lista de clientes conectados

    public ClientHandler(Socket socket, Chatters clientes) {
        // Asignar los objetos que llegan a su respectivo atributo en la clase
        this.clientSocket = socket;
        this.clientes = clientes;

        // Crear canales de entrada in y de salida out para la comunicación
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            clientName = in.readLine(); // Solicita un nombre de usuario a un cliente
            while (clientes.personExist(clientName)) { // Verifica que el nombre de usuario del nuevo cliente no exista
                out.println("Nombre de usuario ya existente. Por favor, ingrese otro nombre."); // Solicita de nuevo el nombre si ese nombre ya está en uso
                clientName = in.readLine();
            }
            clientes.addPerson(clientName, out); // Añade al cliente al chatters con su canal de salida out
            clientes.sendMessageToAll(clientName + " has joined the chat."); // Notifica a los demás usuarios que hay un nuevo miembro en el chat
            out.println("You have joined the group"); // Notifica al cliente que fue aceptado

            String message;
            while ((message = in.readLine()) != null) {
<<<<<<< HEAD
                if (message.startsWith("@")) {
            // Mensaje privado
                String[] parts = message.split(" ", 2);
            if (parts.length > 1) {
                String recipient = parts[0].substring(1);
                String privateMessage = parts[1];
            clientes.sendMessageToUser(clientName, recipient, privateMessage);
        } else {
            out.println("Invalid private message format. Usage: @recipient message");
        }
    } else {
        // Mensaje público
        clientes.sendMessageToAll(clientName + ": " + message);
    }
}

=======
                clientes.sendMessageToAll(clientName + ": " + message);
            }
>>>>>>> a25aa627be212685dc2b1dbc327fe2513ecd09ed
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            
        }

    }


}