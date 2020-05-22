import java.util.ArrayList;

public class ReceivedPutChunk implements Runnable {
    private String fileId;
    private int chunkNo;
    private int repDeg;
    private byte[] body;
    private Chunk chunk;
    private String senderAddress;
    private int senderPort;

    public ReceivedPutChunk(String fileId, int chunkNo, int repDeg, byte[] body, String senderAddress, int senderPort) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = repDeg;
        this.body = body;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }

    @Override
    public void run() {
        Storage peerStorage = Peer.getStorage();
        String chunkKey = this.fileId+"-"+this.chunkNo;
        MessageFactory messageFactory = new MessageFactory();

        if (peerStorage.getChunkCurrentDegree(chunkKey) < this.repDeg) {

            if (!contains(peerStorage)) {
                if (hasSpace(peerStorage)) {
                    Message msg = messageFactory.storedMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), this.fileId, this.chunkNo);
                    Peer.getThreadExecutor().execute(new SendMessagesManager(msg, this.senderAddress, this.senderPort));
                    msg.printSentMessage();
                }

            }
            else {
                Message msg = messageFactory.putChunkMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), this.chunk, this.repDeg);

                Node succ = Peer.getPeer().getFingerTable()[0];
                Peer.getThreadExecutor().execute(new SendMessagesManager(msg, succ.getAddress(), succ.getPort()));
                msg.printSentMessage();
            }
        }
    }

    public boolean contains(Storage storage) {
        ArrayList<Chunk> storedChunks = storage.getStoredChunks();

        for (int i = 0; i < storedChunks.size(); i++) {
            Chunk chunk = storedChunks.get(i);
            if (chunk.getFile_id().equals(this.fileId) && chunk.getChunk_no() == this.chunkNo) {
                this.chunk = chunk;
                return true;
            }
        }

        return false;
    }

    public boolean hasSpace(Storage storage) {
        if (storage.getFreeSpace() >= this.body.length) {
            Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);
            storage.storeChunk(chunk);
            return true;
        }
        else return false;
    }
}
