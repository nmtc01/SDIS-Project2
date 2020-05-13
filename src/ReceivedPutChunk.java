import java.net.DatagramPacket;

public class ReceivedPutChunk implements Runnable {
    private String version;
    private String fileId;
    private int chunkNo;
    private int repDeg;
    private byte[] body;

    public ReceivedPutChunk(String version, String fileId, int chunkNo, int repDeg, byte[] body) {
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDeg = repDeg;
        this.body = body;
    }

    @Override
    public void run() {
        if(manageStorage()) {
            MessageFactory messageFactory = new MessageFactory();
            byte msg[] = messageFactory.storedMsg(this.version, PeerProtocol.getPeer().getPeer_id(), this.fileId, this.chunkNo);
            DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
            new Thread(new SendMessagesManager(sendPacket)).start();
            System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
        }
    }

    public boolean manageStorage() {
        Storage peerStorage = PeerProtocol.getPeer().getStorage();
        String chunkKey = this.fileId+"-"+this.chunkNo;

        if (!PeerProtocol.getProtocol_version().equals("1.0")) {
            if (peerStorage.getChunkCurrentDegree(chunkKey) < this.repDeg && peerStorage.getFreeSpace() > this.body.length) {
                Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);
                peerStorage.storeChunk(chunk);
                return true;
            } else return false;
        }
        else if (peerStorage.getFreeSpace() > this.body.length){
            Chunk chunk = new Chunk(this.fileId, this.chunkNo, this.body.length, this.repDeg, this.body);
            peerStorage.storeChunk(chunk);
            return true;
        }
        else return false;
    }
}
