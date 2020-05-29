import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Node implements java.io.Serializable {

    private int port;
    private String address;
    private BigInteger id;

    public Node(String address, int port) {
        this.port = port;
        this.address = address;
        try {
            byte[] sha1 =  Utility.sha1(this.address+":"+this.port);
           // Integer val = Utility.convertToInt( sha1);

            //ByteBuffer bb= ByteBuffer.wrap(sha1);
            //return bb.getInt();

            this.id = new BigInteger(1,sha1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //TO DEBUG USE THIS
    public Node(int order, String address, int port) {

        this.port = port;
        this.address = address;
        this.id = new BigInteger(String.valueOf(order));

    }
    //TO DEBUG USE THIS
    public Node(BigInteger id, String address, int port) {

        this.port = port;
        this.address = address;
        this.id = id;

    }

    public BigInteger getNodeId() {
        return id;
    }

    public String getAddress(){return this.address; }
    public int getPort(){return port; }

    ///// MESSAGES ////

    public Node requestFindSucc(BigInteger msgId, String ip, int port, BigInteger id) {

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        Message message = messageFactory.findSuccMsg(msgId,ip,port,id);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestFindPred(BigInteger msgId, String address, int port){
        //Create socket

        MessageFactory messageFactory = new MessageFactory();
        Message message = messageFactory.findPredMsg(msgId,address,port);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestFindSuccFinger(BigInteger msgId, String ip, int port, BigInteger id, int fingerId) {

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        Message message = messageFactory.findSuccFingerMsg(msgId,ip,port,id,fingerId);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestNotify(BigInteger msgId, Node node){

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        Message message = messageFactory.notifyMsg(msgId,node.getAddress(),node.getPort());
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

}
