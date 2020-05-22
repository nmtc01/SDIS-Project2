public class SendMessagesManager implements Runnable {
    private Message message;
    private SSLConnection sslConnection;

    public SendMessagesManager(Message message, String ipAddress, int port) {
        this.message = message;
        this.sslConnection = new SSLConnection(ipAddress,port);
    }

    @Override
    public void run(){
        this.sslConnection.send(message);
    }
}