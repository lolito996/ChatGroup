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
                                out.println("\n[System]: Group Already Exists");
                            }else{
                                groups.add(new Group(groupName));
                                out.println("\n[System]: Group Created Succesfully");
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
                            String personLeftGroup = clientName + " has left the group.";
                            clientes.sendMessageToAllInGroup(personLeftGroup,g1.getPersons());
                            out.println("\n[System]: You are no Longer in the Group");
                        }else{
                            String groupString = "\n Enter Group Name :";
                            groupString += groupController.listGroups(); // Obtiene la lista de todos los grupos creados
                            out.println(groupString);
                            String enteringGroup;
                            int index;
                            while((enteringGroup = in.readLine())!=null){
                                index = groupController.searchGroup(enteringGroup); 
                                if(index==-1){
                                    out.println("\n[System]: Group Doesn't Exists");
                                }else{
                                    Person p = clientes.getPerson(clientName);
                                    if(p.isInGroup()){
                                        out.println("\n[System]: You are Already in A Group");
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
                                    out.println("[System]: Invalid private message format. Usage: @recipient message");
                                }
            
                            }else {

                                // Mensaje público
                                Person per1 = clientes.getPerson(clientName);
                                if(per1.isInGroup()){
                                    Group group = per1.getGroup();
                                    newMessage = "\n"+clientName+": "+newMessage;
                                    clientes.sendMessageToAllInGroup(newMessage,group.getPersons());
                                }else{
                                    out.println("\n [System]: You are not in a group yet.....");

                                }
                                
                            }
                            //print("\n TestMessage 4!!!!");
                            break;
                        }
                        //print("\nEnd of Message Sending");
                        break;
                    case "0":
                        Person personLeaving = clientes.getPerson(clientName);
                        if(personLeaving.isInGroup()){
                            personLeaving.setIsInGroup(false);
                            Group lastGroup = personLeaving.getGroup();
                            lastGroup.deletePersonFromGroup(personLeaving);
                            String personleavingMessage = clientName + " has left the group.";
                            clientes.sendMessageToAllInGroup(personleavingMessage,lastGroup.getPersons());
                        }
                        clientes.removeClient(personLeaving);
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
                       out.println("\n[System]: Invalid Option");
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
        String groupOption = "";
        if(clientes.getPerson(clientName).isInGroup()){
           groupOption = "2. Leave Group\n";
        }else{
            groupOption = "2. Join Group\n";
        }
        return "\nMain Menu :\n"+
        "1. Create Group\n"+
        groupOption+
        "3. Send Message to group\n"+
        "4. List Groups\n"+
        "0. Exit Program\n";
        
    }
    public static void print(Object o){System.out.println(o);}

}