import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class Node {

    private int port;
    private String address;
    private BigInteger id;

    public Node(String address, int port) {
        this.port = port;
        this.address = address;
        try {
            this.id = new BigInteger(Utility.sha256(this.address+":"+this.port));
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
        byte[] message = messageFactory.findSuccMsg(msgId,ip,port,id);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestFindPred(BigInteger msgId, String address, int port){
        //Create socket

        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.findPredMsg(msgId,address,port);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestFindSuccFinger(BigInteger msgId, String ip, int port, BigInteger id, int fingerId) {

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.findSuccFingerMsg(msgId,ip,port,id,fingerId);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

    public Node requestNotify(BigInteger msgId, Node node){

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.notifyMsg(msgId,node.getAddress(),node.getPort());
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return null;
    }

}
