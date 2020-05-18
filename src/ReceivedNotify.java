public class ReceivedNotify implements Runnable {

    Node node;

    public ReceivedNotify(String address, int port) {
        this.node = new Node(address, port);
    }

    @Override
    public void run() {
        Peer.getPeer().notify(this.node);
    }
}
