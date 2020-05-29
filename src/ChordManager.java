public class ChordManager implements Runnable {
    Peer peer;

    public  ChordManager(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {

        //System.out.println("SUCC CHECK - " + Peer.succNode.getNodeId());
        if (Peer.succNode.getNodeId().equals(peer.getNodeId()))
            return;

        peer.stabilize();
        peer.fixFingers();
        if(Peer.predNode != null)
            peer.checkPred();

        peer.printFingerTable();

    }
}
