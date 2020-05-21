public class ReceivedDelete implements Runnable {
    String fileId;
    private String senderAddress;
    private int senderPort;

    public ReceivedDelete(String fileId, String senderAddress, int senderPort) {
        this.fileId = fileId;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }

    @Override
    public void run() {
        Storage peerStorage = Peer.getStorage();
        peerStorage.deleteChunk(fileId);
    }
}
