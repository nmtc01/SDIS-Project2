import java.io.File;

public class RestoreChunks implements Runnable {
    private String filename;

    public RestoreChunks(String filename) {
        this.filename = filename;
    }

    @Override
    public void run() {
        Storage peerStorage = PeerProtocol.getPeer().getStorage();
        File file = new File(this.filename);
        peerStorage.restoreFile(file);
    }
}
