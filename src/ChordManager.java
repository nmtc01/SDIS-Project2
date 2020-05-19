public class ChordManager implements Runnable {

    Peer peer;

    public  ChordManager(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        if (Peer.succNode.getNodeId().equals(peer.getNodeId()))
            return;

        peer.stabilize();
        peer.fixFingers();

       // peer.checkPred();

        peer.printFingerTable();

    }
}
