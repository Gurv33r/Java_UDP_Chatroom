package cs158;

public class Message {
    private int type;
    private String username, content;
    public Message(){
        this.type = 0;
        this.username = "";
        this.content="";
    }
    public Message(int type, String username, String content){
        this.type = type;
        this.username = username;
        this.content = content;
    }
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

    public void setUsername(String username) {
        this.username = username;
    }
}
