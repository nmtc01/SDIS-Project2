import java.io.File;

public class RestoreChunks implements Runnable {
    private String filename;
    private final int chunkNo;

    public RestoreChunks(String filename, int chunkNo) {
        this.filename = filename;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        while(chunkNo != Peer.getStorage().getRestoreChunks().size()) {}

        Storage peerStorage = Peer.getStorage();
        File file = new File(this.filename);
        peerStorage.restoreFile(file);
    }
}
