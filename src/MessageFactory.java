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

    //PUTCHUNK <FileId> <ChunkNo> <ReplicationDeg> <SenderAddress> <SenderPort>
    public Message putChunkMsg(String senderAddress, int senderPort, Chunk chunk, Integer replication_degree) {

        String[] header = new String[6];
        header[0] = "PUTCHUNK";
        header[1] = chunk.getFile_id();
        header[2] = Integer.toString(chunk.getChunk_no());
        header[3] = Integer.toString(replication_degree);
        header[4] = senderAddress;
        header[5] = Integer.toString(senderPort);

        byte[] body = chunk.getContent();
        Message putchunkMsg = new Message(header, body);

        this.messageString = "PUTCHUNK" + " " + chunk.getFile_id() + " " + chunk.getChunk_no() + " " + replication_degree + " " + senderAddress + " " + senderPort;

        return putchunkMsg;
    }

    //STORED <FileId> <ChunkNo> <SenderAddress> <SenderPort>
    public Message storedMsg(String senderAddress, int senderPort, String fileId, int chunkNo) {

        String[] header = new String[5];
        header[0] = "STORED";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Message storedMsg = new Message(header);

        this.messageString = "STORED" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return storedMsg;
    }

    //GETCHUNK <FileId> <ChunkNo> <SenderAddress> <SenderPort>
    public Message getChunkMsg(String senderAddress, int senderPort, String fileId, int chunkNo) {

        String[] header = new String[5];
        header[0] = "GETCHUNK";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Message getChunkMsg = new Message(header);

        this.messageString = "GETCHUNK" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return getChunkMsg;
    }

    //CHUNK <FileId> <ChunkNo> <SenderAddress> <SenderPort>
    public Message chunkMsg(String senderAddress, int senderPort, String fileId, int chunkNo, byte[] body) {

        String[] header = new String[5];
        header[0] = "CHUNK";
        header[1] = fileId;
        header[2] = Integer.toString(chunkNo);
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Message chunkMsg = new Message(header, body);

        this.messageString = "CHUNK" + " " + fileId + " " + chunkNo + " " + senderAddress + " " + senderPort;

        return chunkMsg;
    }

    //DELETE <FileId> <SenderAddress> <SenderPort>
    public Message deleteMsg(String senderAddress, int senderPort, Chunk chunk) {

        String[] header = new String[4];
        header[0] = "DELETE";
        header[1] = chunk.getFile_id();
        header[2] = senderAddress;
        header[3] = Integer.toString(senderPort);

        Message deleteMsg = new Message(header);

        this.messageString = "DELETE" + " " + chunk.getFile_id() + " " + senderAddress + " " + senderPort;

        return deleteMsg;
    }

    //REMOVED <FileId> <ChunkNo> <SenderAddress> <SenderPort>
    public Message reclaimMsg(String senderAddress, int senderPort, Chunk chunk) {

        String[] header = new String[5];
        header[0] = "REMOVED";
        header[1] = chunk.getFile_id();
        header[2] = Integer.toString(chunk.getChunk_no());
        header[3] = senderAddress;
        header[4] = Integer.toString(senderPort);

        Message reclaimMsg = new Message(header);

        this.messageString = "REMOVED" + " " + chunk.getFile_id() + " " + chunk.getChunk_no() + " " + senderAddress + " " + senderPort;

        return reclaimMsg;
    }

    //////////////////////
    /// CHORD MESSAGES ///
    //////////////////////

    //FINDSUCC <SenderId> <ReqIpAdress> <ReqPort> <ReqId>
    public Message findSuccMsg(BigInteger msgId, String ip, int port, BigInteger id) {
        String[] header = new String[5];
        header[0] = "FINDSUCC";
        header[1] = msgId.toString();
        header[2] = ip;
        header[3] = Integer.toString(port);
        header[4] = id.toString();

        Message findSucc = new Message(header);

        this.messageString = "FINDSUCC "+msgId+" "+ip+" "+port+" "+id;

        return findSucc;
    }

    //SUCC <SenderId> <SuccId> <SuccAddress> <SuccPort>
    public Message replySuccMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort) {

        String[] header = new String[5];
        header[0] = "SUCC";
        header[1] = msgId.toString();
        header[2] = succId.toString();
        header[3] = succAddress;
        header[4] = Integer.toString(succPort);

        Message replySucc = new Message(header);

        this.messageString = "SUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort;

        return replySucc;
    }

    //FINDSUCCFINGER <SenderId> <ReqIpAdress> <ReqPort> <ReqId> <FingerId>
    public Message findSuccFingerMsg(BigInteger msgId, String ip, int port, BigInteger id, int fingerId) {

        String[] header = new String[6];
        header[0] = "FINDSUCCFINGER";
        header[1] = msgId.toString();
        header[2] = ip;
        header[3] = Integer.toString(port);
        header[4] = id.toString();
        header[5] = Integer.toString(fingerId);

        Message replySucc = new Message(header);

        this.messageString = "FINDSUCCFINGER "+msgId+" "+ip+" "+port+" "+id+ " "+fingerId;

        return replySucc;

    }

    //FINGERSUCC <SenderId> <SuccId> <SuccAddress> <SuccPort> <FingerId>
    public Message replySuccFingerMsg(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {

        String[] header = new String[6];
        header[0] = "FINGERSUCC";
        header[1] = msgId.toString();
        header[2] = succId.toString();
        header[3] = succAddress;
        header[4] = Integer.toString(succPort);
        header[5] = Integer.toString(fingerId);

        Message replySucc = new Message(header);

        this.messageString = "FINGERSUCC " + msgId+ " "+ succId+" "+succAddress+" "+succPort +" "+fingerId;

        return replySucc;
    }

    //NOTIFY <SenderId> <ReqIpAdress> <ReqPort>
    public Message notifyMsg(BigInteger msgId, String address, int port) {

        String[] header = new String[4];
        header[0] = "NOTIFY";
        header[1] = msgId.toString();
        header[2] = address;
        header[3] = Integer.toString(port);

        Message notify = new Message(header);

        this.messageString = "NOTIFY " + msgId+ " "+address+" "+port;

        return notify;
    }

    //FINDPRED <SenderId> <IpAdress> <Port>
    public Message findPredMsg(BigInteger msgId, String address, int port) {

        String[] header = new String[4];
        header[0] = "FINDPRED";
        header[1] = msgId.toString();
        header[2] = address;
        header[3] = Integer.toString(port);

        Message findPredMsg = new Message(header);

        this.messageString = "FINDPRED " + msgId+" "+address+" "+port;

        return findPredMsg;
    }

    //PRED <SenderId> <PredId> <PredAddress> <PredPort>
    public Message predMsg(BigInteger msgId, BigInteger predId, String predAddress, int predPort) {

        String[] header = new String[5];
        header[0] = "PRED";
        header[1] = msgId.toString();
        header[2] = predId.toString();
        header[3] = predAddress;
        header[4] = Integer.toString(predPort);

        Message predMsg = new Message(header);

        this.messageString = "PRED " + msgId+" "+predId+" "+predAddress+" "+predPort;

        return predMsg;
    }

    //CHECKPRED
    public Message checkPredMsg() {
        String[] header = new String[5];
        header[0] = "CHECKPRED";

        Message checkPredMsg = new Message(header);

        this.messageString = "CHECKPRED";

        return checkPredMsg;
    }

}
