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

    private static byte[] _0 = {97};
    private static byte[] _1 = {98};
    private static byte[] _2 = {99};
    private static byte[] _3 = {100};
    private static byte[] _4 = {101};
    private static byte[] _5 = {102};
    private static byte[] _6 = {103};
    private static byte[] _7 = {104};
    private static byte[] _8 = {105};
    private static byte[] _9 = {106};
    private static byte[] _10 = {107};
    private static byte[] _11 = {108};
    private static byte[] _12 = {109};
    private static byte[] _13 = {110};
    private static byte[] _14 = {111};
    private static byte[] _15 = {112};

    private static byte[][] possibleId = {_0,_1,_2,_3,_4,_5,_6,_7,_8,_9,_10,_11,_12,_13,_14,_15};

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

    //debug
    public Node(int order, String address, int port) {

        this.port = port;
        this.address = address;
        this.id = new BigInteger(possibleId[order]);

    }

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

    //TODO
    public Node requestFindPred(){
        //Create socket
        try {
            //InetAddress host_name = InetAddress.getByName(address);
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(this.address, this.port);

            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

            //TODO check this
            String request = "FINDPRED " + "\n";
            out.println(request);
            String reply = in.readLine();

            //TODO see what to do with reply
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO missing returns
        return null;
    }

    public Node requestNotify(BigInteger requestId, Node node){

        return null;
    }

    public boolean testResponse(BigInteger id, String ip, int port){

        //Create socket
        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.testMsg(this.id,ip,port);
        Peer.getThreadExecutor().execute(new SendMessagesManager(message, this.address, this.port));

        return true;
    }

}
