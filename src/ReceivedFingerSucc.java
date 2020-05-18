import java.math.BigInteger;

public class ReceivedFingerSucc implements Runnable {

    BigInteger msgId;
    Node succNode;
    int fingerId;


    public ReceivedFingerSucc(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {
        this.msgId = msgId;
        this.fingerId= fingerId;
        this.succNode = new Node(succId, succAddress, succPort);
        //this.succId = new Node(succAddress, succPort); //todo this should be the stuff

    }

    @Override
    public void run() {
        Peer.updateFinger(succNode,fingerId);
    }
}
