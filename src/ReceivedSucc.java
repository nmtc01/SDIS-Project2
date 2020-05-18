import java.math.BigInteger;

public class ReceivedSucc implements Runnable{

    BigInteger msgId;
    Node succNode;

    public ReceivedSucc(BigInteger msgId, BigInteger succId, String succAddress, int succPort){
        this.msgId = msgId;
        this.succNode = new Node(succId, succAddress, succPort);
        //this.succId = new Node(succAddress, succPort); //todo this should be the stuff
    }

    @Override
    public void run() {
        Peer.setSuccNode(this.succNode);
    }
}
