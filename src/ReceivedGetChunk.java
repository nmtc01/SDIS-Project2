import java.net.DatagramPacket;

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
                sendCommonChunk(chunk);
            }
        }
    }

    public void sendCommonChunk(Chunk chunk) {
        MessageFactory messageFactory = new MessageFactory();
        byte msg[] = messageFactory.chunkMsg(Peer.getPeer_id(), this.fileId, this.chunkNo, chunk.getContent());
        DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
        String chunkKey = this.fileId+"-"+this.chunkNo;
        if (!Peer.getStorage().getRestoreChunks().containsKey(chunkKey)) {
            new Thread(new SendMessagesManager(sendPacket)).start();
            System.out.printf("Sent message: %s\n", messageFactory.getMessageString());
        }
    }

    /*public void sendEnhChunk(Chunk chunk) {
        MessageFactory messageFactory = new MessageFactory();
        byte msg[] = messageFactory.chunkEnhMsg(PeerProtocol.getPeer().getPeer_id(), this.fileId, this.chunkNo);
        DatagramPacket headerPacket = new DatagramPacket(msg, msg.length);
        String chunkKey = this.fileId+"-"+this.chunkNo;
        if (!PeerProtocol.getPeer().getStorage().getRestoreChunks().containsKey(chunkKey)) {
            new Thread(new SendMessagesManager(headerPacket)).start();
        }

        DatagramPacket bodyPacket = new DatagramPacket(chunk.getContent(), chunk.getContent().length);
        String key = this.fileId+"-"+this.chunkNo;
        if (!PeerProtocol.getPeer().getStorage().getRestoreChunks().containsKey(key)) {
            int port = 4444 + PeerProtocol.getPeer().getPeer_id() + this.chunkNo + Integer.valueOf(fileId.charAt(0));
            new Thread(new SendRestoreEnh(bodyPacket, port)).start();
            System.out.printf("Sent enhanced message: %s\n", messageFactory.getMessageString());
        }
    }*/
}
