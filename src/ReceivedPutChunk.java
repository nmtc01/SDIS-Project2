public class ReceivedPutChunk implements Runnable {
    private String fileId;
    private int chunkNo;
    private int repDeg;
    private byte[] body;

    public ReceivedPutChunk(String fileId, int chunkNo, int repDeg, byte[] body) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = repDeg;
        this.body = body;
    }

    @Override
    public void run() {
        if(manageStorage()) {
            MessageFactory messageFactory = new MessageFactory();
            byte msg[] = messageFactory.storedMsg(this.fileId, this.chunkNo);
            new Thread(new SendMessagesManager(msg)).start();
            System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
        }
    }

    public boolean manageStorage() {
        Storage peerStorage = Peer.getStorage();
        String chunkKey = this.fileId+"-"+this.chunkNo;

        if (peerStorage.getChunkCurrentDegree(chunkKey) < this.repDeg && peerStorage.getFreeSpace() > this.body.length) {
            Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);
            peerStorage.storeChunk(chunk);
            return true;
        }
        else return false;
    }
}
