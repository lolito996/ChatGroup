package Server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

class ClientHandler implements Runnable {

    //private BufferedReader in; // Flujo de entrada para leer los mensajes del cliente
    //private PrintWriter out; // Flujo de salida para enviar mensajes al cliente
    private ObjectInputStream inputStream; // Flujo de entrada de entrada de mensajes
    private ObjectOutputStream outputStream; //Flujo de salida de mensajes
    private String clientName; // Nombre de usuario del cliente
    private GroupController groupController;
    private ArrayList<Group> groups; //lista de grupos creados
    private Chatters clientes; // Objeto que contiene la lista de clientes conectados
    

    public ClientHandler(Socket socket, Chatters clientes, ArrayList<Group> groups) {
        this.clientes = clientes;
        this.groups = groups;
        this.groupController = new GroupController(groups);

        // Crear canales de entrada in y de salida out para la comunicación
        try {
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //out = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void run() {

        try {
            Object optionOpject;
            String newClientName;
            while((optionOpject = inputStream.readObject())!=null){
                newClientName = (String)optionOpject;
                if(clientes.personExist(newClientName) || newClientName.equalsIgnoreCase("all")){
                    outputStream.writeObject("0");
                }else{
                    clientName = newClientName;
                    outputStream.writeObject("1");
                    break;
                }
            }
            clientes.addPerson(clientName, outputStream); 
            
            outputStream.writeObject("------------WELCOME---------------");

            outputStream.writeObject(mainMenu());

            executeProgram();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

    }
    private void executeProgram(){
        try{
            String message;
            while ((message = (String)inputStream.readObject()) != null) {
                switch(message){
                    case "1":
                        createGroup();
                        break;
                    case "2":
                        if(clientes.getPerson(clientName).isInGroup()){
                            removePersonFromGroup();
                        }else{
                            addPersonToGroup();
                        }
                        break;
                    case "3":
                        sendMessage();
                        break;
                    case "4":
                        sendAudio();
                        break;
                    case "5":
                        String users = clientes.listUsers();
                        outputStream.writeObject(users);
                        break;
                    case "6":
                        startCall();
                        break;
                    case "0":
                        clientExitProgram();
                        break;
                    default:
                        outputStream.writeObject("\n[System]: Invalid Option");
                }
                try{
                    Thread.sleep(1000);
                    outputStream.writeObject(mainMenu());
                }catch(Exception e){}       
            }
        }catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    private void createGroup() throws IOException, ClassNotFoundException{
        outputStream.writeObject("\n Enter Group Name :");
        String groupName;
        while((groupName = (String)inputStream.readObject())!=null){
            if(groupController.groupExists(groupName)){
                outputStream.writeObject("\n[System]: Group Already Exists");
            }else{
                groups.add(new Group(groupName));
                outputStream.writeObject("\n[System]: Group Created Succesfully");
            }
            break;
        }
    }
    private void removePersonFromGroup() throws IOException{
        Person p1 = clientes.getPerson(clientName);
        Group g1 = p1.getGroup();
        g1.removePersonFromGroup(p1);
        p1.setIsInGroup(false);
        String personLeftGroup = clientName + " has left the group.";
        clientes.sendNotificationToAllInGroup(personLeftGroup,g1);
        outputStream.writeObject("\n[System]: You are no Longer in the Group");
    }
    private void addPersonToGroup() throws IOException, ClassNotFoundException{
        String groupString = "\n Enter Group Name :";
        groupString += groupController.listGroups(); // Obtiene la lista de todos los grupos creados
        outputStream.writeObject(groupString);
        String enteringGroup;
        int index;
        while((enteringGroup = (String)inputStream.readObject())!=null){
            index = groupController.searchGroup(enteringGroup); 
            if(index==-1){
                outputStream.writeObject("\n[System]: Group Doesn't Exists");
            }else{
                Person p = clientes.getPerson(clientName);
                if(p.isInGroup()){
                    outputStream.writeObject("\n[System]: You are Already in A Group");
                }else{
                    groupController.addClientToGroup(index,p);
                    String msj3 = clientName + " has joined the group.";
                    clientes.sendNotificationToAllInGroup(msj3,groups.get(index));
                }
                
            }
            break;
        }
    }
    private void sendMessage() throws IOException, ClassNotFoundException{
        outputStream.writeObject("\n Type Message (@username --> for private message ):");
        String newMessage;
        while((newMessage = (String)inputStream.readObject())!=null){
            if (newMessage.startsWith("@")) {
                // Mensaje privado
                sendMessageToUser(newMessage);
            }else {
                sendMessageToGroup(newMessage); 
            }
            break;
        }
    }
    private void sendMessageToUser(String message) throws IOException{
        String[] parts = message.split(" ", 2);
        if (parts.length > 1) {
            String recipient = parts[0].substring(1);
            String privateMessage = parts[1];
            clientes.sendMessageToUser(clientName, recipient, privateMessage);

        } else {
            outputStream.writeObject("\n[System]: Invalid private message format. Usage: @recipient message");
        }
    }
    private void sendMessageToGroup(String message) throws IOException{
        Person per1 = clientes.getPerson(clientName);
        if(per1.isInGroup()){
            Group group = per1.getGroup();
            clientes.sendMessageToAllInGroup(clientName,message,group);
        }else{
            outputStream.writeObject("\n [System]: You are not in a group yet.....");
        }
    }
    private void startCall() throws ClassNotFoundException, IOException{
        Object receivedObj;
        while ((receivedObj = inputStream.readObject())!=null) {

            if (receivedObj instanceof Call) {
                Call call = (Call) receivedObj;
                String receiverName = call.getReceiver();
                if(clientes.personExist(receiverName)){

                    Person receiver = clientes.getPerson(receiverName);
                    ObjectOutputStream recipientStream = receiver.getOutputStream();
                    if (recipientStream != null) {
                        recipientStream.writeObject(call);
                    }
                }else{
                    outputStream.writeObject("[System] : User Not Found");
                }
            }else if(receivedObj instanceof EndingFlag){
                break;
            }else{
                outputStream.writeObject("\n[System] : Audio not received by server");
                break;
            }
        }
        outputStream.writeObject("\nCall Ended");
    }
    private void sendAudio() throws IOException, ClassNotFoundException{
        Object receivedObj;
        while ((receivedObj = inputStream.readObject())!=null) {

            if (receivedObj instanceof VoiceNote) {

                VoiceNote voiceNote = (VoiceNote) receivedObj;
                String receiverName = voiceNote.getReceiver();

                if (receiverName.equalsIgnoreCase("all")) {
                    // Sending voice note to all clients except sender
                    Person sender = clientes.getPerson(clientName);
                    if(sender.isInGroup()){
                        Group group = sender.getGroup();
                        for (Person p : group.getPersons()) {
                            ObjectOutputStream clientStream = p.getOutputStream();
                            if (clientStream != outputStream) {
                                clientStream.writeObject(voiceNote);
                            }
                        }
                    }else{
                        outputStream.writeObject("\n[System]: You are not in a group yet...");
                    }
                    
                } else {
                    Person receiver = clientes.getPerson(receiverName);
                    if(receiver!=null){

                        ObjectOutputStream recipientStream = receiver.getOutputStream();
                        if (recipientStream != null) {
                            recipientStream.writeObject(voiceNote);
                        }
                    }else{
                        outputStream.writeObject("\n[System] : User Not found or offline");
                    }
                    
                    // Sending voice note to specific client
                }
                break;
            }else{
                outputStream.writeObject("\n[System] : Audio not received by server");
                break;
            }
        }
    }
    private void clientExitProgram() throws IOException{
        Person personLeaving = clientes.getPerson(clientName);
        if(personLeaving.isInGroup()){
            personLeaving.setIsInGroup(false);
            Group lastGroup = personLeaving.getGroup();
            lastGroup.deletePersonFromGroup(personLeaving);
            String personleavingMessage = clientName + " has left the group.";
            clientes.sendNotificationToAllInGroup(personleavingMessage,lastGroup);
        }
        clientes.removeClient(personLeaving);
        outputStream.writeObject("\n See you Next Time!");
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
        "3. Send Message\n"+
        "4. Send Audio\n"+
        "5. Print all Users\n"+
        "6. Start a call\n"+
        "7. Send Saved Song\n"+
        "0. Exit Program\n";
        
    }

}