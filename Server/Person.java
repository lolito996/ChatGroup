package Server;
import java.io.ObjectOutputStream;


//objeto que representa un cliente o usuario o persona en el chat
public class Person {

    private Group group;
    private Boolean isInGroup;
    private String name; // Nombre de usuario
    //PrintWriter out;    // Canal para enviarle mensajes a ese usuario
    private ObjectOutputStream outputStream; //Canal para enviar mensajes y audio a este usuario

    public Person(String name, ObjectOutputStream newOutputStream){
        this.name = name;
        //this.out  = out;
        this.outputStream = newOutputStream;
        this.isInGroup = false;
        this.group = null;
    }
    public Group getGroup(){
        return this.group;
    }

    public Boolean isInGroup(){
        return this.isInGroup;
    }
    public void addToGroup(Group g){
        this.group = g;
    }
   
    public void setIsInGroup(boolean boolVariable){
        this.isInGroup = boolVariable;
    }
    public String getName() {
        return this.name;
    }
    public ObjectOutputStream getOutputStream(){
        return this.outputStream;
    }
}
