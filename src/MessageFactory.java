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
    public Request putChunkMsg(String senderAddress, int senderPort, Chunk chunk, Integer replication_degree) {

        String[] header = new String[6];
        header[0] = "PUTCHUNK";
        header[1] = chunk.getFile_id();
        header[2] = Integer.toString(chunk.getChunk_no());
        header[3] = Integer.toString(replication_degree);
        header[4] = senderAddress;
        header[5] = Integer.toString(senderPort);

        byte[] body = chunk.getContent();
        Request putchunkMsg = new Request(header, body);

        this.messageString = "PUTCHUNK" + " " + chunk.getFile_id() + " " + chunk.getChunk_no() + " " + replication_degree + " " + senderAddress + " " + senderPort;

        return putchunkMsg;
    }

    //STORED <FileId> <ChunkNo> <CRLF><CRLF>
    public Request storedMsg(String senderAddress, int senderPort, String fileId, int chunkNo) {

        String[] header = new String[5];
        header[0] = "STORED";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Request storedMsg = new Request(header);

        this.messageString = "STORED" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return storedMsg;
    }

    //GETCHUNK <FileId> <ChunkNo> <CRLF><CRLF>
    public Request getChunkMsg(String senderAddress, int senderPort, String fileId, int chunkNo) {

        String[] header = new String[5];
        header[0] = "GETCHUNK";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Request getChunkMsg = new Request(header);

        this.messageString = "GETCHUNK" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return getChunkMsg;
    }

    //CHUNK <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public Request chunkMsg(String senderAddress, int senderPort, String fileId, int chunkNo, byte[] body) {

        String[] header = new String[5];
        header[0] = "CHUNK";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Request chunkMsg = new Request(header, body);

        this.messageString = "CHUNK" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return chunkMsg;
    }

    //DELETE <FileId> <CRLF><CRLF>
    public Request deleteMsg(String senderAddress, int senderPort, Chunk chunk) {

        String[] header = new String[5];
        header[0] = "DELETE";
        header[1] = chunk.getFile_id();
        header[2] = Integer.toString(chunk.getChunk_no());
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Request deleteMsg = new Request(header);

        this.messageString = "DELETE" + " " + chunk.getFile_id() + " " + senderAddress + " " + senderPort;

        return deleteMsg;
    }

    //REMOVED <FileId> <ChunkNo> <CRLF><CRLF>
    public Request reclaimMsg(String senderAddress, int senderPort, Chunk chunk) {

        String[] header = new String[5];
        header[0] = "REMOVED";
        header[1] = chunk.getFile_id();
        header[2] = Integer.toString(chunk.getChunk_no());
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Request reclaimMsg = new Request(header);

        this.messageString = "REMOVED" + " " + chunk.getFile_id() + " " + chunk.getChunk_no() + " " + senderAddress + " " + senderPort;

        return reclaimMsg;
    }

    //////////////////////
    /// CHORD MESSAGES ///
    //////////////////////

    //FINDSUCC <SenderId> <ReqIpAdress> <ReqPort> <ReqId> <CRLF><CRLF>
    public Request findSuccMsg(BigInteger msgId, String ip, int port, BigInteger id) {
        String[] header = new String[5];
        header[0] = "FINDSUCC";
        header[1] = msgId.toString();
        header[2] = ip;
        header[3] = Integer.toString(port);
        header[4] = id.toString();

        Request findSucc = new Request(header);

        this.messageString = "FINDSUCC "+msgId+" "+ip+" "+port+" "+id;

        return findSucc;
    }

    //SUCC <SenderId> <SuccId> <SuccAddress> <SuccPort> <CRLF><CRLF>
    public Request replySuccMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort) {

        String[] header = new String[5];
        header[0] = "SUCC";
        header[1] = msgId.toString();
        header[2] = succId.toString();
        header[3] = succAddress;
        header[4] = Integer.toString(succPort);

        Request replySucc = new Request(header);

        this.messageString = "SUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort;

        return replySucc;
    }

    //FINDSUCCFINGER <SenderId> <ReqIpAdress> <ReqPort> <ReqId> <FingerId> <CRLF><CRLF>
    public Request findSuccFingerMsg(BigInteger msgId, String ip, int port, BigInteger id, int fingerId) {

        String[] header = new String[6];
        header[0] = "FINDSUCCFINGER";
        header[1] = msgId.toString();
        header[2] = ip;
        header[3] = Integer.toString(port);
        header[4] = id.toString();
        header[5] = Integer.toString(fingerId);

        Request replySucc = new Request(header);

        this.messageString = "FINDSUCCFINGER "+msgId+" "+ip+" "+port+" "+id+ " "+fingerId;

        return replySucc;

    }

    //FINGERSUCC <SenderId> <SuccId> <SuccAddress> <SuccPort> <FingerId> <CRLF><CRLF>
    public Request replySuccFingerMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {

        String[] header = new String[6];
        header[0] = "FINGERSUCC";
        header[1] = msgId.toString();
        header[2] = succId.toString();
        header[3] = succAddress;
        header[4] = Integer.toString(succPort);
        header[5] = Integer.toString(fingerId);

        Request replySucc = new Request(header);

        this.messageString = "FINGERSUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort +" "+fingerId;

        return replySucc;
    }

    //NOTIFY <SenderId> <ReqIpAdress> <ReqPort> <CRLF><CRLF>
    public Request notifyMsg(BigInteger msgId, String address, int port) {

        String[] header = new String[4];
        header[0] = "NOTIFY";
        header[1] = msgId.toString();
        header[2] = address;
        header[3] = Integer.toString(port);

        Request notify = new Request(header);

        this.messageString = "NOTIFY " + msgId+ " "+address+" "+port;

        return notify;
    }

    //FINDPRED <SenderId> <IpAdress> <Port>
    public Request findPredMsg(BigInteger msgId,String address, int port) {

        String[] header = new String[4];
        header[0] = "FINDPRED";
        header[1] = msgId.toString();
        header[2] = address;
        header[3] = Integer.toString(port);

        Request findPredMsg = new Request(header);

        this.messageString = "FINDPRED " + msgId+" "+address+" "+port;

        return findPredMsg;
    }

    //PRED <SenderId> <PredId> <PredAddress> <PredPort>
    public Request predMsg(BigInteger msgId,BigInteger predId, String predAddress, int predPort) {

        String[] header = new String[5];
        header[0] = "PRED";
        header[1] = msgId.toString();
        header[2] = predId.toString();
        header[3] = predAddress;
        header[4] = Integer.toString(predPort);

        Request predMsg = new Request(header);

        this.messageString = "PRED " + msgId+" "+predId+" "+predAddress+" "+predPort;

        return predMsg;
    }
}
