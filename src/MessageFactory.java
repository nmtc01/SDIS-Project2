import java.math.BigInteger;

public class MessageFactory {
    private String messageString;

    public MessageFactory() {}

    public String getMessageString() {
        return messageString;
    }

    ////////////////////////////////
    /// BACKUP PROTOCOL MESSAGES ///
    ////////////////////////////////

    //PUTCHUNK <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    public byte[] putChunkMsg(Chunk chunk, Integer replication_degree) {

        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = "PUTCHUNK" + " " + fileId + " " + chunkNo + " " + replication_degree;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] content = chunk.getContent();
        byte[] putchunkMsg = new byte[header.length + content.length];
        System.arraycopy(header, 0, putchunkMsg, 0, header.length);
        System.arraycopy(content, 0, putchunkMsg, header.length, content.length);

        return putchunkMsg;
    }

    //STORED <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] storedMsg(String fileId, int chunkNo) {

        this.messageString = "STORED" + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] storedMsg = new byte[header.length];
        System.arraycopy(header, 0, storedMsg, 0, header.length);

        return storedMsg;
    }

    //GETCHUNK <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] getChunkMsg(String fileId, int chunkNo) {

        this.messageString = "GETCHUNK" + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] getChunkMsg = new byte[header.length + header.length];
        System.arraycopy(header, 0, getChunkMsg, 0, header.length);

        return getChunkMsg;
    }

    //CHUNK <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public byte[] chunkMsg(String fileId, int chunkNo, byte[] body) {
        this.messageString = "CHUNK" + " " + fileId + " " + chunkNo;
        String headerTerms = this.messageString + " \r\n\r\n";
        byte[] header = headerTerms.getBytes();
        byte[] chunkMsg = new byte[header.length + body.length];
        System.arraycopy(header, 0, chunkMsg, 0, header.length);
        System.arraycopy(body, 0, chunkMsg, header.length, body.length);

        return chunkMsg;
    }

    //DELETE <FileId> <CRLF><CRLF>
    public byte[] deleteMsg(Chunk chunk) {

        String fileId = chunk.getFile_id();
        this.messageString = "DELETE" + " " + fileId;
        String deleteString = this.messageString + " \r\n\r\n";
        byte[] header = deleteString.getBytes();
        byte[] deleteMsg = new byte[header.length];
        System.arraycopy(header, 0, deleteMsg, 0, header.length);

        return deleteMsg;
    }

    //REMOVED <FileId> <ChunkNo> <CRLF><CRLF>
    public byte[] reclaimMsg(Chunk chunk) {

        String fileId = chunk.getFile_id();
        int chunkNo = chunk.getChunk_no();
        this.messageString = "REMOVED" + " " + fileId + " " + chunkNo;
        String reclaimString = this.messageString + " \r\n\r\n";
        byte[] header = reclaimString.getBytes();
        byte[] reclaimMsg = new byte[header.length];
        System.arraycopy(header, 0, reclaimMsg, 0, header.length);

        return reclaimMsg;
    }

    //////////////////////
    /// CHORD MESSAGES ///
    //////////////////////

    //FINDSUCC <SenderId> <ReqIpAdress> <ReqPort> <ReqId> <CRLF><CRLF>
    public byte[] findSuccMsg(BigInteger msgId, String ip, int port, BigInteger id) {
        this.messageString = "FINDSUCC "+msgId+" "+ip+" "+port+" "+id;

        String request = this.messageString +" \r\n\r\n";
        byte[] header = request.getBytes();
        byte[] findSucc = new byte[header.length];
        System.arraycopy(header, 0, findSucc, 0, header.length);

        return findSucc;
    }

    //SUCC <SenderId> <SuccId> <SuccAddress> <SuccPort> <CRLF><CRLF>
    public byte[] replySuccMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort) {

        this.messageString = "SUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort;

        String request = this.messageString+" \r\n\r\n";
        byte[] header = request.getBytes();
        byte[] replySucc = new byte[header.length];
        System.arraycopy(header,0,replySucc,0,header.length);

        return replySucc;
    }

    //FINDSUCCFINGER <SenderId> <ReqIpAdress> <ReqPort> <ReqId> <FingerId> <CRLF><CRLF>
    public byte[] findSuccFingerMsg(BigInteger msgId, String ip, int port, BigInteger id, int fingerId) {

        this.messageString = "FINDSUCCFINGER "+msgId+" "+ip+" "+port+" "+id+ " "+fingerId+" \r\n\r\n";

        String request = this.messageString;
        byte[] header = request.getBytes();
        byte[] findSucc = new byte[header.length];
        System.arraycopy(header, 0, findSucc, 0, header.length);

        return findSucc;

    }
    //FINGERSUCC <SenderId> <SuccId> <SuccAddress> <SuccPort> <FingerId> <CRLF><CRLF>
    public byte[] replySuccFingerMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {

        this.messageString = "FINGERSUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort +" "+fingerId+" \r\n\r\n";

        String request = this.messageString;
        byte[] header = request.getBytes();
        byte[] replySucc = new byte[header.length];
        System.arraycopy(header,0,replySucc,0,header.length);

        return replySucc;
    }

    //TEST <SenderId> <ReqIpAdress> <ReqPort> <CRLF><CRLF>
    public byte[] notifyMsg(BigInteger msgId, String address, int port) {

        this.messageString = "NOTIFY " + msgId+ " "+address+" "+port+" \r\n\r\n";

        String request = this.messageString;
        byte[] header = request.getBytes();
        byte[] notify = new byte[header.length];
        System.arraycopy(header,0,notify,0,header.length);

        return notify;
    }

    //TEST <SenderId> <ReqIpAdress> <ReqPort> <CRLF><CRLF>
    public byte[] testMsg(BigInteger msgId, String succAddress, int succPort) {

        this.messageString = "TEST " + msgId+ " "+succAddress+" "+succPort;

        String request = this.messageString+" \r\n\r\n";
        byte[] header = request.getBytes();
        byte[] test = new byte[header.length];
        System.arraycopy(header,0,test,0,header.length);

        return test;
    }

    //REPTEST <SenderId>
    public byte[] replyTestMsg(BigInteger msgId) {

        this.messageString = "REPTEST " + msgId;

        String request = this.messageString+ " \r\n\r\n";
        byte[] header = request.getBytes();
        byte[] replyTest = new byte[header.length];
        System.arraycopy(header,0,replyTest,0,header.length);

        return replyTest;
    }

    //FINDPRED <SenderId> <IpAdress> <Port>
    public byte[] findPredMsg(BigInteger msgId,String address, int port) {

        this.messageString = "FINDPRED " + msgId+" "+address+" "+port+ " \r\n\r\n";

        String request = this.messageString;
        byte[] header = request.getBytes();
        byte[] replyTest = new byte[header.length];
        System.arraycopy(header,0,replyTest,0,header.length);

        return replyTest;
    }

    //PRED <SenderId> <PredId> <PredAddress> <PredPort>
    public byte[] predMsg(BigInteger msgId,BigInteger predId, String predAddress, int predPort) {

        this.messageString = "PRED " + msgId+" "+predId+" "+predAddress+" "+predPort+ " \r\n\r\n";

        String request = this.messageString;
        byte[] header = request.getBytes();
        byte[] replyTest = new byte[header.length];
        System.arraycopy(header,0,replyTest,0,header.length);

        return replyTest;
    }
}
