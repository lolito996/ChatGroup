package Server;

import java.io.Serializable;

public class Call implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String receiver;

    private byte[] voiceData;


    public Call(String receiver,String sender, byte[] voiceData) {
        this.sender = sender;
        this.receiver = receiver;
        this.voiceData = voiceData;
    }


    public String getSender() {
        return this.sender;
    }
    public String getReceiver(){
        return this.receiver;
    }

    public byte[] getVoiceData() {
        return this.voiceData;
    }
    
}
