import javax.net.ssl.SSLSocket;
import java.math.BigInteger;

public class ReceivedFindSucc implements Runnable {

    String address;
    int port;
    BigInteger id;

    ReceivedFindSucc(String address, int port, BigInteger id) {

        this.address = address;
        this.port = port;
        this.id = id;
    }

    @Override
    public void run() {
        Node node = Peer.getPeer().findSucc(this.address, this.port, this.id);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccMsg(Peer.getPeer().getNodeId(),node.getNodeId());

            SendMessagesManager sendMessagesManager = new SendMessagesManager(message);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }


    }
}
