package Server;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;


public class Chatters {
    
    private Set<Person> clientes = new HashSet<>(); // Lista de personas que serán nuestros clientes
    private ArrayList<Person> personList = new ArrayList<>();
    Scanner scanner = new Scanner(System.in);
    private static final String AUDIO_FOLDER = "audios";

    public Chatters() {
        createAudioFolderIfNeeded();
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
    public void removeClient(Person person){
        clientes.remove(person);
        personList.remove(person);
    }
    public Person getPerson(String name){
        Person person = null;
        for(int i=0;i<personList.size();i++){
            if(personList.get(i).getName().equals(name)){
                person = personList.get(i);
            }
        }
        return person;
    }
    public int getSize(){
        return clientes.size();
    }

    // Método para agregar un usuario nuevo
    public void addPerson(String nombre, PrintWriter out) {
        Person p = new Person(nombre, out);
        clientes.add(p);
        personList.add(p);
    }

    // Método para enviar un mensaje a todos los usuarios
    public void sendMessageToAll(String mensaje) {
        for (Person p : clientes) {
            p.getOut().println(mensaje);
        }
    }
    public void sendMessageToAllInGroup(String mensaje,ArrayList<Person> persons) {
        for (Person p : persons) {
            p.getOut().println(mensaje);
        }
    }
    // Método para enviar un mensaje a un usuario específico
    public void sendMessageToUser(String sender, String recipient, String message) {
        for (Person p : clientes) {
            if (p.getName().equals(recipient)) {
                p.getOut().println("\n"+sender + " (private): " + message);
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
    /*
    public byte[] startRecording(String username) throws LineUnavailableException, IOException{
        System.out.print("Press Enter to start recording...");
        scanner.nextLine();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            // Verifica si el sistema soporta la línea de entrada de audio
            System.err.println("Line not supported");
            System.exit(0);
        }
        try (TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info)) {
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            Thread recordingThread = new Thread(() -> {
                // Graba audio continuamente hasta que el usuario detiene la grabación
                int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
                byte[] buffer = new byte[bufferSize];
                while (true) {
                    int count = targetDataLine.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        byteArrayOutputStream.write(buffer, 0, count);
                    }
                }
            });
            recordingThread.start();
            // Espera a que el usuario detenga la grabación
            System.out.println("Recording... Press Enter to stop and send");
            scanner.nextLine();
            // Detiene la grabación y cierra la línea de entrada de audio
            targetDataLine.stop();
            targetDataLine.close();
            // Guarda el audio en un archivo y lo envía al servidor
            saveAudio(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }
    /* */
    // Verifica si la carpeta de audio existe, si no, la crea
    private static void createAudioFolderIfNeeded() {
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
    public void sendAudioToUser(String recipient,String sender,byte[] audioData ){
        for (Person p : clientes) {
            if (p.getName().equals(recipient)) {
                p.getOut().println(sender + " (private Audio): ");
                playAudio(audioData);
                return;
            }
        }
        // Enviar mensaje al remitente si el destinatario no se encuentra
        sendMessageToSender(sender, "User '" + recipient + "' not found or offline.");
    }
    public void sendAudioToAll(String sender,byte[] audioData,ArrayList<Person> persons ){
            for (Person p : persons) {
                p.out.println("Audio enviado por"+ sender);
                playAudio(audioData);
            }
        
    }


    // Reproduce los datos de audio
    public static void playAudio(byte[] audioData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
             AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, getAudioFormat(), audioData.length / getAudioFormat().getFrameSize())) {

            // Abre una línea de salida de audio
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(getAudioFormat());
            sourceDataLine.open(getAudioFormat());
            sourceDataLine.start();

            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            // Lee datos de audio del flujo de entrada y los escribe en la línea de salida
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            // Drena la línea de salida y la cierra
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    // Obtiene el formato de audio PCM firmado con las especificaciones proporcionadas
    private static AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    }
}
