import java.util.ArrayList;

public class ReceivedPutChunk implements Runnable {
    private String fileId;
    private int chunkNo;
    private int repDeg;
    private byte[] body;
    private Chunk chunk;

    public ReceivedPutChunk(String fileId, int chunkNo, int repDeg, byte[] body) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = repDeg;
        this.body = body;
    }

    @Override
    public void run() {
        Storage peerStorage = Peer.getStorage();
        String chunkKey = this.fileId+"-"+this.chunkNo;
        MessageFactory messageFactory = new MessageFactory();

        if (peerStorage.getChunkCurrentDegree(chunkKey) < this.repDeg) {

            if (!contains(peerStorage)) {

                if (hasSpace(peerStorage)) {
                    byte msg[] = messageFactory.storedMsg(this.fileId, this.chunkNo);
                    new Thread(new SendMessagesManager(msg)).start();
                    System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
                }

            }
            else {
                byte msg[] = messageFactory.putChunkMsg(this.chunk, this.repDeg);

                Node succ = Peer.getPeer().getFingerTable()[0];
                Peer.getThreadExecutor().execute(new SendMessagesManager(msg, succ.getAddress(), succ.getPort()));
                System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
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
        if (storage.getFreeSpace() > this.body.length) {
            Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);
            storage.storeChunk(chunk);
            return true;
        }
        else return false;
    }
}
