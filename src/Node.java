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

        //TODO missing returns
        return null;
    }

    //TODO
    public Node requestFindPred(){
        //Create socket
        try {
            InetAddress host_name = InetAddress.getByName(address);
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host_name, port);

            /*if (cypher_suite.length > 0) {
                sslSocket.setEnabledCipherSuites(cypher_suite);
            }*/

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

    public boolean testResponse(BigInteger id){

        //test during a certain time to communicate with the predecessor, if it succeeds return false if it fails return true;
        //function to test connection has failed on peer
        return true;
    }

}
