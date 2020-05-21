public class SendMessagesManager implements Runnable {
    private Message message;
    private SSLConnection sslConnection;

    public SendMessagesManager(Message message, String ipAddress, int port) {
        this.message = message;
        this.sslConnection = new SSLConnection(ipAddress,port);
    }

    public SendMessagesManager(Message message) {
        this.message = message;
    }

    @Override
    public void run(){
        this.sslConnection.send(message);
    }
}