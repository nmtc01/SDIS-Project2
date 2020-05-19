public class ChordManager implements Runnable {

    Peer peer;

    public  ChordManager(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {

        peer.stabilize();
        peer.fixFingers();

       // peer.checkPred();

        peer.printFingerTable();

    }
}
