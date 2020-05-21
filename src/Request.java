public class Request {
    private String[] header;
    private byte[] body;

    public Request(String[] header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public Request(String[] header) {
        this.header = header;
    }


    public String[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }
}
