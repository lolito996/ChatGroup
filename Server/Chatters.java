package Server;
import java.util.Set;
import java.io.PrintWriter;
import java.util.HashSet;

public class Chatters {
    
    private Set<Person> clientes = new HashSet<>(); // Lista de personas que serán nuestros clientes

    public Chatters() {
    }

    // Método para verificar si un usuario existe, retorna true si existe
    public boolean personExist(String nombre) {
        for (Person p : clientes) {
            if (p.getName().equals(nombre)) {
                return true;
            }
        }
        return false;
    }
    public int getSize(){
        return clientes.size();
    }

    // Método para agregar un usuario nuevo
    public void addPerson(String nombre, PrintWriter out) {
        Person p = new Person(nombre, out);
        clientes.add(p);
    }

    // Método para enviar un mensaje a todos los usuarios
    public void sendMessageToAll(String mensaje) {
        for (Person p : clientes) {
            p.getOut().println(mensaje);
        }
    }
    // Método para enviar un mensaje a un usuario específico
    public void sendMessageToUser(String sender, String recipient, String message) {
        for (Person p : clientes) {
            if (p.getName().equals(recipient)) {
                p.getOut().println(sender + " (private): " + message);
                return;
            }
        }
        // Enviar mensaje al remitente si el destinatario no se encuentra
        sendMessageToSender(sender, "User '" + recipient + "' not found or offline.");
    }

// Método para enviar un mensaje de error al remitente
    private void sendMessageToSender(String sender, String message) {
        for (Person p : clientes) {
            if (p.getName().equals(sender)) {
                p.getOut().println(message);
                return;
            }
        }
    }

}