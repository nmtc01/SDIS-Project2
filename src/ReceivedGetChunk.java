public class ReceivedGetChunk implements Runnable {
    private String fileId;
    private int chunkNo;

    public ReceivedGetChunk(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
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
        MessageFactory messageFactory = new MessageFactory();
        byte msg[] = messageFactory.chunkMsg(Peer.getPeer_id(), this.fileId, this.chunkNo, chunk.getContent());
        String chunkKey = this.fileId+"-"+this.chunkNo;
        if (!Peer.getStorage().getRestoreChunks().containsKey(chunkKey)) {
            new Thread(new SendMessagesManager(msg)).start();
            System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
        }
    }
}
