public class MessageFactory {
    private String messageString;

    public MessageFactory() {}

    public String getMessageString() {
        return messageString;
    }

    //PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    public byte[] putChunkMsg(Chunk chunk, Integer replication_degree, int peer_id) {

        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = "PUTCHUNK" + " " + peer_id + " " + fileId + " " + chunkNo + " " + replication_degree;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] content = chunk.getContent();
        byte[] putchunkMsg = new byte[header.length + content.length];
        System.arraycopy(header, 0, putchunkMsg, 0, header.length);
        System.arraycopy(content, 0, putchunkMsg, header.length, content.length);

        return putchunkMsg;
    }

    //STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] storedMsg(int senderId, String fileId, int chunkNo) {

        this.messageString = "STORED" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] storedMsg = new byte[header.length];
        System.arraycopy(header, 0, storedMsg, 0, header.length);

        return storedMsg;
    }

    //GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] getChunkMsg(int senderId, String fileId, int chunkNo) {

        this.messageString = "GETCHUNK" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] getChunkMsg = new byte[header.length + header.length];
        System.arraycopy(header, 0, getChunkMsg, 0, header.length);

        return getChunkMsg;
    }

    //CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public byte[] chunkMsg(int senderId, String fileId, int chunkNo, byte[] body) {
        this.messageString = "CHUNK" + " " + senderId + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] chunkMsg = new byte[header.length + body.length];
        System.arraycopy(header, 0, chunkMsg, 0, header.length);
        System.arraycopy(body, 0, chunkMsg, header.length, body.length);

        return chunkMsg;
    }

    //DELETE <SenderId> <FileId> <CRLF><CRLF>
    public byte[] deleteMsg(Chunk chunk, int senderId) {

        String fileId = chunk.getFile_id();
        this.messageString = "DELETE" + " " + senderId + " " + fileId;
        String deleteString = this.messageString + " \r\n\r\n";
        byte[] header = deleteString.getBytes();
        byte[] deleteMsg = new byte[header.length];
        System.arraycopy(header, 0, deleteMsg, 0, header.length);

        return deleteMsg;
    }

    //REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] reclaimMsg(Chunk chunk, int senderId) {

        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = "REMOVED" + " " + senderId + " " + fileId + " " + chunkNo;
        String reclaimString = this.messageString + " \r\n\r\n";
        byte[] header = reclaimString.getBytes();
        byte[] reclaimMsg = new byte[header.length];
        System.arraycopy(header, 0, reclaimMsg, 0, header.length);

        return reclaimMsg;
    }

    public byte[] findSuccMsg(/*NodeId*/) {
        this.messageString = "FINDSUCC " /*+ node.getNodeId()*/ + " \n";
        String request = this.messageString;
        return request.getBytes();
    }

    public byte[] replySuccMsg() {
        this.messageString = "SUCC " + /* NODE ID*/ " \n";
        String request = this.messageString;
        return request.getBytes();
    }

}
