import java.io.Serializable;

public class Message implements Serializable {
    private String[] header;
    private byte[] body;

    public Message(String[] header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public Message(String[] header) {
        this.header = header;
    }


    public String[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }
}
