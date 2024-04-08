package Client;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import Server.VoiceNote;
import Server.Call;
import Server.EndingFlag;
public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3500;
    private static final String AUDIO_FOLDER = "audios";

    private Scanner scanner;
    private String username;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private BufferedReader userInput;
    private Socket socket;
    public Client(){
        try{
            socket = new Socket(SERVER_IP, PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            userInput = new BufferedReader(new InputStreamReader(System.in));
            scanner = new Scanner(System.in); //Eliminar después
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    public static void main(String[] args) throws InterruptedException{
        Client main = new Client();
        main.receiveClient();
    }
    private void receiveClient(){
        try {
            System.out.println("connection established with the server");
            System.out.print("Type username : ");
            String flag = "0";
            while(flag.equals("0")){
                String newUsername = userInput.readLine();
                outputStream.writeObject(newUsername);
                //out.println(newUsername);
                String response = (String)inputStream.readObject();
                if(response.equals("0")){
                    System.out.println("\n[System] : Username Already Taken");
                    flag="0";
                }else{
                    flag="1";
                    username = newUsername;
                }
            }

            //Response del WELCOME
            String response = (String)inputStream.readObject();
            System.out.println(response);

            //Response de Main Menu
            response = (String)inputStream.readObject();
            System.out.println(response);
                       
            Thread receiverThread = new Thread(() -> {
                try {
                    Object receivedObj;
                    while ((receivedObj = inputStream.readObject())!= null) {
                        
                        if (receivedObj instanceof VoiceNote) {
                            // Recibe la nota de voz y la reproduce
                            VoiceNote voiceNote = (VoiceNote) receivedObj;
                            System.out.println("\n Voice note received from " + voiceNote.getSender());
                            playAudio(voiceNote.getVoiceData());

                        }else if(receivedObj instanceof String){
                            System.out.println((String)receivedObj);

                        }else if(receivedObj instanceof Call){
                            Call call = (Call) receivedObj;
                            playAudio(call.getVoiceData());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            receiverThread.start();

            String option = "";
            do{
                option = userInput.readLine();
                outputStream.writeObject(option);
                switch(option){
                    case "1":
                        String groupName;
                        while ((groupName = userInput.readLine()) != null){
                            outputStream.writeObject(groupName);
                            outputStream.flush();
                            break;
                        }
                        break;
                    case "2":
                    String enteringGroup;
                        while ((enteringGroup = userInput.readLine()) != null){
                            outputStream.writeObject(enteringGroup);
                            outputStream.flush();
                            break;
                        }
                        break;
                    case "3":
                        String userInputMessage;
                        while ((userInputMessage = userInput.readLine()) != null){
                            outputStream.writeObject(userInputMessage);
                            outputStream.flush();
                            break;
                        }
                        break;
                    case "4":
                        recordAudio();  
                        break;
                    case "6":
                        startCall();
                        break;
                    case "7":
                    sendSong();
                        break;
                    case "0":
                        Thread.sleep(200);
                        break;
                    default:
                }

            }while(!option.equals("0"));

            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendSong(){
        
    }

    private void startCall() throws IOException{
        System.out.println("Who are you Calling?");
        String recipient = userInput.readLine();
        // Inicia la grabación de audio cuando el usuario presiona Enter
        System.out.print("\nPress Enter to start Talking...");
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
                        try {
                            byteArrayOutputStream.write(buffer, 0, count);
                            Call call = new Call(recipient,username,byteArrayOutputStream.toByteArray());
                            outputStream.writeObject(call);
                            //buffer = new byte[bufferSize];
                            byteArrayOutputStream.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            recordingThread.start();

            // Espera a que el usuario detenga la grabación
            System.out.print("Talk... Press Enter to finish call");
            scanner.nextLine();

            // Detiene la grabación y cierra la línea de entrada de audio
            targetDataLine.stop();
            targetDataLine.close();
            recordingThread.interrupt();
            // Guarda el audio en un archivo y lo envía al servidor

            byteArrayOutputStream.close();
            outputStream.writeObject(new EndingFlag());
            
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
    
    private void playAudio(byte[] audioData) {
        try (
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, getAudioFormat(), audioData.length / getAudioFormat().getFrameSize())) {
            // Abre una línea de salida de audio
            System.out.println("SizeData: "+audioData.length);
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
    
    @SuppressWarnings("deprecation")
    private void recordAudio(){
        // Solicita al usuario que ingrese el destinatario de la nota de voz
        System.out.print("Enter recipient username (or 'all' for group): ");
        String recipient = scanner.nextLine();

        // Inicia la grabación de audio cuando el usuario presiona Enter
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
            System.out.print("Recording... Press Enter to stop and send");
            scanner.nextLine();

            // Detiene la grabación y cierra la línea de entrada de audio
            targetDataLine.stop();
            targetDataLine.close();
            recordingThread.suspend();

            // Guarda el audio en un archivo y lo envía al servidor
            saveAudio(byteArrayOutputStream.toByteArray());
            VoiceNote voiceNote = new VoiceNote(recipient,username,byteArrayOutputStream.toByteArray());
            outputStream.writeObject(voiceNote);
            byteArrayOutputStream.close();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
    private void saveAudio(byte[] audioData) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Obtiene el formato de audio PCM firmado con las especificaciones proporcionadas
    private AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    }

}