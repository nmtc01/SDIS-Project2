import javax.net.ssl.SSLSocket;
import java.math.BigInteger;

public class ReceivedFindSucc implements Runnable {
    String address;
    int port;
    BigInteger requestId;
    BigInteger idToFind;

    ReceivedFindSucc(String address, int port, BigInteger requestId, BigInteger idToFind) {
        this.address = address;
        this.port = port;
        this.requestId = requestId;
        this.idToFind = idToFind;
    }

    @Override
    public void run() {
        Node node = Peer.getPeer().findSucc(requestId, idToFind);

        if (node != null) {
            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccMsg();
            SendMessagesManager sendMessagesManager = new SendMessagesManager(message);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }


    }
}
