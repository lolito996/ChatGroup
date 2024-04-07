package Server;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;


//objeto que representa un cliente o usuario o persona en el chat
public class Person {

    private Group group;
    private byte[] audioData;
    private AudioRecorderPlayer audioRecorder;
    private Boolean isInGroup;
    private String name; // Nombre de usuario
    PrintWriter out;    // Canal para enviarle mensajes a ese usuario
    private ObjectOutputStream outputStream;

    public Person(String name, PrintWriter out, ObjectOutputStream newOutputStream){
        this.name = name;
        this.out  = out;
        this.outputStream = newOutputStream;
        this.isInGroup = false;
        this.group = null;
        this.audioData = new byte[4096];
        this.audioRecorder = new AudioRecorderPlayer();
    }
    public Group getGroup(){
        return this.group;
    }
    public void setAudioData(byte[] newAudioData){
        this.audioData = newAudioData;
    }
    public byte[] getAudioData(){
        return this.audioData;
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
    
    public PrintWriter getOut() {
        return this.out;
    }
    public ObjectOutputStream getOutputStream(){
        return this.outputStream;
    }
    public AudioRecorderPlayer getAudioRecorder() {
        return this.audioRecorder;
    }
}
