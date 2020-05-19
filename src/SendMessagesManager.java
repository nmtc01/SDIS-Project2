import javax.imageio.IIOException;

public class SendMessagesManager implements Runnable {
    private byte[] message;
    private SSLConnection sslConnection;

    public SendMessagesManager(byte[] message, String ipAddress, int port) {
        this.message = message;
        this.sslConnection = new SSLConnection(ipAddress,port);
    }

    public SendMessagesManager(byte[] message) {
        this.message = message;
    }

    @Override
    public void run(){
        this.sslConnection.send(message);
    }
}