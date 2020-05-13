public class ReceivedDelete implements Runnable {
    String fileId;

    public ReceivedDelete(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public void run() {
        Storage peerStorage = Peer.getStorage();
        peerStorage.deleteChunk(fileId);
    }
}
