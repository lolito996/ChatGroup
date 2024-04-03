package Client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientAudio {
    // Dirección IP del servidor
    private static final String SERVER_IP = "localhost";
    // Puerto del servidor
    private static final int SERVER_PORT = 12345;
    // Carpeta donde se guardarán los archivos de audio
    private static final String AUDIO_FOLDER = "audios";

    public static void main(String[] args) {
        // Verifica si la carpeta de audio existe, si no, la crea
        createAudioFolderIfNeeded();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server...");
            // Solicita al usuario que ingrese su nombre de usuario
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            // Envía el nombre de usuario al servidor
            outputStream.writeObject(username);

            // Hilo para recibir notas de voz del servidor
            Thread receiverThread = new Thread(() -> {
                try {
                    while (true) {
                        Object receivedObj = inputStream.readObject();
                        if (receivedObj instanceof VoiceNote) {
                            // Recibe la nota de voz y la reproduce
                            VoiceNote voiceNote = (VoiceNote) receivedObj;
                            System.out.println("Voice note received from " + voiceNote.getSender());
                            playAudio(voiceNote.getVoiceData());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            receiverThread.start();

            while (true) {
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

                    // Guarda el audio en un archivo y lo envía al servidor
                    saveAudio(byteArrayOutputStream.toByteArray());
                    VoiceNote voiceNote = new VoiceNote(username, byteArrayOutputStream.toByteArray());
                    outputStream.writeObject(voiceNote);
                    byteArrayOutputStream.close();
                } catch (LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
    private static void saveAudio(byte[] audioData) {
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

    // Reproduce los datos de audio
    private static void playAudio(byte[] audioData) {
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