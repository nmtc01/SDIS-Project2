import java.net.InetAddress;
import java.net.UnknownHostException;

public class MessageFactory {
    private String messageString;

    public MessageFactory() {}

    public String getMessageString() {
        return messageString;
    }

    //<Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    public byte[] putChunkMsg(Chunk chunk, Integer replication_degree, int peer_id) {

        String version = PeerProtocol.getProtocol_version();
        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = version + " " + "PUTCHUNK" + " " + peer_id + " " + fileId + " " + chunkNo + " " + replication_degree;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] content = chunk.getContent();
        byte[] putchunkMsg = new byte[header.length + content.length];
        System.arraycopy(header, 0, putchunkMsg, 0, header.length);
        System.arraycopy(content, 0, putchunkMsg, header.length, content.length);

        return putchunkMsg;
    }

    //<Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] storedMsg(String version, int senderId, String fileId, int chunkNo) {

        this.messageString = version + " " + "STORED" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] storedMsg = new byte[header.length];
        System.arraycopy(header, 0, storedMsg, 0, header.length);

        return storedMsg;
    }

    //<Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] getChunkMsg(String version, int senderId, String fileId, int chunkNo) {

        this.messageString = version + " " + "GETCHUNK" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] getChunkMsg = new byte[header.length + header.length];
        System.arraycopy(header, 0, getChunkMsg, 0, header.length);

        return getChunkMsg;
    }

    //<Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public byte[] chunkMsg(String version, int senderId, String fileId, int chunkNo, byte[] body) {
        this.messageString = version + " " + "CHUNK" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] chunkMsg = new byte[header.length + body.length];
        System.arraycopy(header, 0, chunkMsg, 0, header.length);
        System.arraycopy(body, 0, chunkMsg, header.length, body.length);

        return chunkMsg;
    }

    //<Version> CHUNKENH <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] chunkEnhMsg(String version, int senderId, String fileId, int chunkNo) {
        this.messageString = version + " " + "CHUNK" + " " + senderId + " " + fileId + " " + chunkNo;
        try {
            String headerTerms = this.messageString + " \r\n\r\n" + InetAddress.getLocalHost().getHostAddress();
            byte[] header = headerTerms.getBytes();
            return header;
        }catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    //<Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
    public byte[] deleteMsg(Chunk chunk, int senderId) {

        String version = PeerProtocol.getProtocol_version();
        String fileId = chunk.getFile_id();
        this.messageString = version + " " + "DELETE" + " " + senderId + " " + fileId;
        String deleteString = this.messageString + " \r\n\r\n";
        byte[] header = deleteString.getBytes();
        byte[] deleteMsg = new byte[header.length];
        System.arraycopy(header, 0, deleteMsg, 0, header.length);

        return deleteMsg;
    }

    //<Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] reclaimMsg(Chunk chunk, int senderId) {

        String version = PeerProtocol.getProtocol_version();
        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = version + " " + "REMOVED" + " " + senderId + " " + fileId + " " + chunkNo;
        String reclaimString = this.messageString + " \r\n\r\n";
        byte[] header = reclaimString.getBytes();
        byte[] reclaimMsg = new byte[header.length];
        System.arraycopy(header, 0, reclaimMsg, 0, header.length);

        return reclaimMsg;
    }

    //<Version> AWAKE <SenderId> <CRLF><CRLF>
    public byte[] awakeMsg(int senderId) {
        String version = PeerProtocol.getProtocol_version();
        this.messageString = version + " " + "AWAKE" + " " + senderId;
        String deleteString = this.messageString + " \r\n\r\n";
        byte[] awake = deleteString.getBytes();

        return awake;
    }
}
