package Server;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;


public class Chatters {
    
    private Set<Person> clientes = new HashSet<>(); // Lista de personas que serán nuestros clientes
    private ArrayList<String> messages;
    private static final String AUDIO_FOLDER = "audios";

    public Chatters() {
        createAudioFolderIfNeeded();
        messages = new ArrayList<>();
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
    //removeClient : Elimina un cliente de la lista de usuarios
    public void removeClient(Person person){
        clientes.remove(person);
    }
    //getPerson retorna una sola persona buscándola por su nombre
    public Person getPerson(String name){
        Person person = null;
        for(Person p : clientes){
            if(p.getName().equals(name)){
                person = p;
            }   
        }
        /*
        for(int i=0;i<personList.size();i++){
            if(personList.get(i).getName().equals(name)){
                person = personList.get(i);
            }
        }
        */
        return person;
    }
    //getSize : Retorna cuantos usuarios hay registrados
    public int getSize(){
        return clientes.size();
    }

    // Método para agregar un usuario nuevo
    public void addPerson(String nombre, ObjectOutputStream newOutputStream) {
        Person p = new Person(nombre, newOutputStream);
        clientes.add(p);
    }

    // Método para enviar un mensaje a todos los usuarios (No es usado en ningún momento, pero se dejó por si es necesario en el futuro)
    public void sendMessageToAll(String mensaje) throws IOException {
        for (Person p : clientes) {
            p.getOutputStream().writeObject(mensaje);
        }
    }
    //Envía un mensaje a todos los integrantes del grupo del remitente
    public void sendMessageToAllInGroup(String clientName,String mensaje,Group group) {
        ArrayList<Person> persons = group.getPersons();
        mensaje = clientName+": "+mensaje;
        try{
            for (Person p : persons) {
            p.getOutputStream().writeObject("\n"+mensaje);
            }
            saveMessage("("+group.getGroupName()+") "+mensaje);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void sendNotificationToAllInGroup(String message, Group group){
        for (Person p : group.getPersons()) {
            try {
                p.getOutputStream().writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Método para enviar un mensaje a un usuario específico
    public void sendMessageToUser(String sender, String recipient, String message) {
        try{
            for (Person p : clientes) {
                if (p.getName().equals(recipient)) {
                    String sendingMessage = sender + " (private): " + message;
                    String newMessage = sender + " (private to "+recipient+") :"+message;
                    p.getOutputStream().writeObject("\n"+sendingMessage);
                    saveMessage(newMessage);
                    return;
                }
            }
            // Enviar mensaje al remitente si el destinatario no se encuentra
            sendMessageToSender(sender, "User '" + recipient + "' not found or offline.");
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    //saveMessage : Guarda el historial de mensajes en un archivo txt
    public void saveMessage(String msj) throws IOException {
        messages.add(msj);
        String path = "dataMessages.json";
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

        String data = "";
        for(int i=0;i<messages.size();i++){
            data += "["+i+"] "+messages.get(i)+"\n";
        }

        writer.write(data);
        writer.flush();
        fos.close();
    }
    //listUsers: Retorna la lista de todos lo susuarios registrados en el sistema hasta ahora
    public String listUsers(){
        String msj = "\n  USERS :";
        for (Person p : clientes) {
            msj+="\n"+p.getName();
        }
        return msj;
    }

// Método para enviar un mensaje de error al remitente
    private void sendMessageToSender(String sender, String message) throws IOException {
        for (Person p : clientes) {
            if (p.getName().equals(sender)) {
                p.getOutputStream().writeObject(message);
                return;
            }
        }
    }
    // Verifica si la carpeta de audio existe, si no, la crea
    public static void createAudioFolderIfNeeded() {
        Path audioFolderPath = Paths.get(AUDIO_FOLDER);
        if (!Files.exists(audioFolderPath)) {
            try {
                Files.createDirectory(audioFolderPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Guarda los datos de audio en un archivo WAV en la carpeta de audio
    public static void saveAudio(byte[] audioData) {
        try {
            // Obtiene el formato de audio
            AudioFormat audioFormat = getAudioFormat();
            // Crea un AudioInputStream utilizando los datos de audio y el formato de audio
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), audioFormat, audioData.length / audioFormat.getFrameSize());
            // Genera un nombre de archivo único basado en la marca de tiempo actual
            String fileName = System.currentTimeMillis() + ".wav";
            // Define la ruta del archivo de audio
            Path filePath = Paths.get(AUDIO_FOLDER, fileName);
            // Escribe los datos de audio en un archivo WAV
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, filePath.toFile());
            // Muestra un mensaje de confirmación
            System.out.println("Audio saved: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Obtiene el formato de audio PCM firmado con las especificaciones proporcionadas
    private static AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    }
}
