package Server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


class ClientHandler implements Runnable {

    private Socket clientSocket; // Socket para la conexión con el cliente
    private BufferedReader in; // Flujo de entrada para leer los mensajes del cliente
    private PrintWriter out; // Flujo de salida para enviar mensajes al cliente
    private String clientName; // Nombre de usuario del cliente
    private GroupController groupController;
    ArrayList<Group> groups; //lista de grupos creados
    Chatters clientes; // Objeto que contiene la lista de clientes conectados
    

    public ClientHandler(Socket socket, Chatters clientes, ArrayList<Group> groups) {
        // Asignar los objetos que llegan a su respectivo atributo en la clase
        this.clientSocket = socket;
        this.clientes = clientes;
        this.groups = groups;
        this.groupController = new GroupController(groups);

        // Crear canales de entrada in y de salida out para la comunicación
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String mainMenu(){
        return "\n"+
        "1. Create Group\n"+
        "2. Join Group\n"+
        "3. Send Message\n"+
        "4. List Groups";
    }

    @Override
    public void run() {

        try {
            clientName = in.readLine(); // Solicita un nombre de usuario a un cliente
            while (clientes.personExist(clientName)) { // Verifica que el nombre de usuario del nuevo cliente no exista
                out.println("\nUsername already taken. Please enter a new name."); // Solicita de nuevo el nombre si ese nombre ya está en uso
                clientName = in.readLine();
            }
            clientes.addPerson(clientName, out); // Añade al cliente al chatters con su canal de salida out
            clientes.sendMessageToAll(clientName + " has joined the chat."); // Notifica a los demás usuarios que hay un nuevo miembro en el chat
            out.println("You have joined the group"); // Notifica al cliente que fue aceptado
            out.println(mainMenu());
            String message;
            Integer flag =1;
            while ((message = in.readLine()) != null) {

                switch(message){
                    case "1":
                        out.println("\n Enter Group Name :");
                        String groupName;
                        while((groupName = in.readLine())!=null){
                            if(groupController.groupExists(groupName)){
                                out.println("\n Group Already Exists");
                            }else{
                                groups.add(new Group(groupName));
                                out.println("\nGroup Created Succesfully");
                            }
                            break;
                        }
                        break;
                    case "2":
                        break;
                    case "3":
                        out.println("\n Type Message");
                        String newMessage;
                        while((newMessage = in.readLine())!=null){
                            print("MensajeTest 1");
                            if (message.startsWith("@")) {
                                // Mensaje privado
                                String[] parts = newMessage.split(" ", 2);
                                if (parts.length > 1) {
                                    String recipient = parts[0].substring(1);
                                    String privateMessage = parts[1];
                                    clientes.sendMessageToUser(clientName, recipient, privateMessage);
            
                                } else {
                                    out.println("Invalid private message format. Usage: @recipient message");
                                }
            
                            }else {
                                // Mensaje público
                                clientes.sendMessageToAll(clientName + ": " + newMessage);
                            }
                            print("\n TestMessage 4!!!!");
                            break;
                        }
                        print("\nEnd of Message Sending");
                        break;
                    case "0":
                        out.println("See you Next Time!");
                        break;
                    case "4":
                        String msj = "\n";
                        msj += "Group List :";
                        for(int i=0;i<groups.size();i++){
                            msj +="\nNAME : "+groups.get(i).getGroupName();
                        }
                        out.println(msj);
                        break;
                    case "5":
                        out.println("Clientes size : "+clientes.getSize());
                        break;
                    default:
                       print("\n Invalid Option");
                }
                print("\n TestMessage 5!!!!!");
                out.println(mainMenu());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


    }
    public static void print(Object o){System.out.println(o);}

}