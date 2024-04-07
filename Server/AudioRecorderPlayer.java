package Server;
import javax.sound.sampled.*;
import java.io.*;

public class AudioRecorderPlayer {
    private static  int SAMPLE_RATE = 16000; // Frecuencia de muestreo en Hz
    private static  int SAMPLE_SIZE_IN_BITS = 16; // Tamaño de muestra en bits
    private static  int CHANNELS = 2; // Mono
    private static  boolean SIGNED = true; // Muestras firmadas
    private static  boolean BIG_ENDIAN = false; // Little-endian
    private AudioFormat format;
    private ByteArrayOutputStream byteArrayOutputStream;

    public AudioRecorderPlayer(){
        this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public ByteArrayOutputStream recordAudio(){
        int duration = 5; //cuantos segundos vamos a grabar?

        //iniciar objeto de grabacion de audio
        RecordAudio recorder = new RecordAudio(format, duration,byteArrayOutputStream);
        Thread recorderTrh   = new Thread(recorder);
        recorderTrh.start();
        //esperar a que la grabacion termine
        System.out.println("Grabando...");
        try{
            recorderTrh.join();
        }catch(Exception e){
            //TODO
        }

        System.out.println("Grabacion terminada");
        return byteArrayOutputStream;
    }

    public void reproduceAudio(ByteArrayOutputStream byteArrayOutputStream){
        // Reproducir el audio grabado
        byte[] audioData = byteArrayOutputStream.toByteArray();
        PlayerRecording player = new PlayerRecording(format);
        player.initiateAudio(audioData);
    }


}
