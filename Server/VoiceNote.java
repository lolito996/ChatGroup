package Server;
// VoiceNote.java
import java.io.Serializable;

// Esta clase representa una nota de voz que puede ser enviada entre clientes.

public class VoiceNote implements Serializable {
    // Número de versión para la serialización
    private static final long serialVersionUID = 1L;

    // El remitente de la nota de voz
    private String sender;
    private String receiver;

    // Los datos de audio de la nota de voz
    private byte[] voiceData;

    // Constructor de la clase VoiceNote
    // Parámetros:
    //   - sender: El remitente de la nota de voz
    //   - voiceData: Los datos de audio de la nota de voz
    public VoiceNote(String receiver,String sender, byte[] voiceData) {
        this.sender = sender;
        this.receiver = receiver;
        this.voiceData = voiceData;
    }

    // Método para obtener el remitente de la nota de voz.
    // Retorna:
    //   - El remitente de la nota de voz
    public String getSender() {
        return this.sender;
    }
    public String getReceiver(){
        return this.receiver;
    }

    // Método para obtener los datos de audio de la nota de voz.
    // Retorna:
    //   - Los datos de audio de la nota de voz
    public byte[] getVoiceData() {
        return this.voiceData;
    }
}