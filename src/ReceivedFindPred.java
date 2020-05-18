import java.math.BigInteger;

public class ReceivedFindPred implements Runnable{

    BigInteger msgId;
    String address;
    int port;

    public ReceivedFindPred(BigInteger msgId, String address, int port) {
        this.msgId = msgId;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {

        Node node = Peer.findPred();

        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.predMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort());

        SendMessagesManager sendMessagesManager = new SendMessagesManager(message, this.address, this.port);

        Peer.getThreadExecutor().execute(sendMessagesManager);

    }
}
