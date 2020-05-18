import java.math.BigInteger;

public class ReceivedFindFingerSucc implements Runnable{

    int fingerId;
    String address;
    int port;
    BigInteger id;

    public ReceivedFindFingerSucc(String address, int port, int fingerId, BigInteger id) {

        this.address = address;
        this.port = port;
        this.fingerId=fingerId;
        this.id = id;
    }

    @Override
    public void run() {
        Node node = Peer.getPeer().findSuccFinger(this.address, this.port,this.id,this.fingerId);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccFingerMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort(),this.fingerId);

            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, this.address, this.port);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }
    }
}
