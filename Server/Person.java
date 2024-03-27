package Server;
import java.io.PrintWriter;


//objeto que representa un cliente o usuario o persona en el chat
public class Person {

    private String name; // Nombre de usuario
    PrintWriter out;    // Canal para enviarle mensajes a ese usuario

    public Person(String name, PrintWriter out){
        this.name = name;
        this.out  = out;
    }
   
    public String getName() {
        return name;
    }
    
    public PrintWriter getOut() {
        return out;
    }
}
