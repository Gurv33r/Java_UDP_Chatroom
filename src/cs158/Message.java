package cs158;

// used to represent messages between client and server
public class Message {
    private int type;
    private final String username;
    private String content;
    //avoid Object() call
    public Message(){
        this.type = 0;
        this.username = "";
        this.content="";
    }
    // primary Construcor
    public Message(int type, String username, String content){
        this.type = type;
        this.username = username;
        this.content = content;
    }
    // getters and setters
    public int getType(){
        return this.type;
    }
    public String getUsername(){
        return this.username;
    }

    public String getContent() {
        return content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
