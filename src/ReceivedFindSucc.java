import javax.net.ssl.SSLSocket;

public class ReceivedFindSucc implements Runnable {
    SSLSocket sslSocket;

    ReceivedFindSucc(SSLSocket sslSocket) {
        this.sslSocket = sslSocket;
    }

    @Override
    public void run() {
        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.replySuccMsg();

        SendMessagesManager sendMessagesManager = new SendMessagesManager(sslSocket, message);
        Peer.getThreadExecutor().execute(sendMessagesManager);
    }
}
