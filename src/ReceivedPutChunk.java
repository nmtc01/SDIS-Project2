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

        Chunk chunk = new Chunk(this.senderAddress, this.senderPort, this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);

        if (!peerStorage.contains(this.fileId, this.chunkNo) && peerStorage.hasSpace(this.body.length)) {
            peerStorage.storeChunk(chunk);

            Message msg = messageFactory.storedMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), this.fileId, this.chunkNo);
            Peer.getThreadExecutor().execute(new SendMessagesManager(msg, this.senderAddress, this.senderPort));
            msg.printSentMessage();
        }
        else {
            Message msg = messageFactory.putChunkMsg(this.senderAddress, this.senderPort, chunk, this.repDeg);
            Node succ = Peer.getPeer().getFingerTable()[0];
            Peer.getThreadExecutor().execute(new SendMessagesManager(msg, succ.getAddress(), succ.getPort()));
            msg.printSentMessage();
        }

    }
}
