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
    

    @Override
    public void run() {

        try {
            clientName = in.readLine(); // Solicita un nombre de usuario a un cliente
            while (clientes.personExist(clientName)) { // Verifica que el nombre de usuario del nuevo cliente no exista
                out.println("\nUsername already taken. Please enter a new name."); // Solicita de nuevo el nombre si ese nombre ya está en uso
                clientName = in.readLine();
            }
            clientes.addPerson(clientName, out); // Añade al cliente al chatters con su canal de salida out
            //clientes.sendMessageToAll(clientName + " has joined the chat."); 
            out.println("------------WELCOME---------------");
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
                        if(clientes.getPerson(clientName).isInGroup()){
                            Person p1 = clientes.getPerson(clientName);
                            Group g1 = p1.getGroup();
                            g1.removePersonFromGroup(p1);
                            p1.setIsInGroup(false);
                            String msj4 = clientName + " has left the group.";
                            clientes.sendMessageToAllInGroup(msj4,g1.getPersons());
                            out.println("\n You are no Longer in the Group");
                        }else{
                            String msj2 = "\n Enter Group Name :";
                            msj2 += groupController.listGroups();
                            out.println(msj2);
                            String enteringGroup;
                            int index;
                            while((enteringGroup = in.readLine())!=null){
                                index = groupController.searchGroup(enteringGroup); 
                                if(index==-1){
                                    out.println("\n Group Doesn't Exists");
                                }else{
                                    Person p = clientes.getPerson(clientName);
                                    if(p.isInGroup()){
                                        out.println("\n You are Already in A Group");
                                    }else{
                                        groupController.addClientToGroup(index,p);
                                        String msj3 = clientName + " has joined the group.";
                                        clientes.sendMessageToAllInGroup(msj3,groups.get(index).getPersons());
                                    }
                                    
                                }
                                break;
                            }
                        }
                        
                        break;
                    case "3":
                        out.println("\n Type Message :");
                        String newMessage;
                        while((newMessage = in.readLine())!=null){
                            //out.println("Mensaje: "+newMessage);
                            //print("MensajeTest 1");
                            if (newMessage.startsWith("@")) {

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
                                Person per1 = clientes.getPerson(clientName);
                                //out.println("Nombre del cliente :"+per1.getName());
                                //Group g3 = per1.getGroup();
                                Group group = per1.getGroup();
                                newMessage = clientName+": "+newMessage;
                                clientes.sendMessageToAllInGroup(newMessage,group.getPersons());
                            }
                            //print("\n TestMessage 4!!!!");
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
                //print("\n TestMessage 5!!!!!");
                out.println(mainMenu());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


    }
    public String mainMenu(){
        if(clientes.getPerson(clientName).isInGroup()){
            return "\nMain Menu :\n"+
            "1. Create Group\n"+
            "2. Leave Group\n"+
            "3. Send Message to group\n"+
            "4. List Groups";
        }else{
            return "\nMain Menu :\n"+
            "1. Create Group\n"+
            "2. Join Group\n"+
            "3. Send Message to group\n"+
            "4. List Groups";
        }
        
    }
    public static void print(Object o){System.out.println(o);}

}