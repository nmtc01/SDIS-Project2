public class ReceivedGetChunk implements Runnable {
    private String fileId;
    private int chunkNo;
    private String senderAddress;
    private int senderPort;

    public ReceivedGetChunk(String fileId, int chunkNo, String senderAddress, int senderPort) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }

    @Override
    public void run() {
        Storage storage = Peer.getStorage();
        for (int i = 0; i < storage.getStoredChunks().size(); i++) {
            Chunk chunk = storage.getStoredChunks().get(i);
            if (chunk.getFile_id().equals(this.fileId) && chunk.getChunk_no() == this.chunkNo) {
                sendChunk(chunk);
            }
        }
    }

    public void sendChunk(Chunk chunk) {
        String chunkKey = this.fileId+"-"+this.chunkNo;
        if (!Peer.getStorage().getRestoreChunks().containsKey(chunkKey)) {
            MessageFactory messageFactory = new MessageFactory();
            Message msg = messageFactory.chunkMsg(this.senderAddress, this.senderPort, this.fileId, this.chunkNo, chunk.getContent());
            Peer.getThreadExecutor().execute(new SendMessagesManager(msg, this.senderAddress, this.senderPort));
            msg.printSentMessage();
        }
    }
}
