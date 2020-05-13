import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class Node {
    private int port;
    private String address;
    private String id;

    public Node(String address, int port) {
        this.port = port;
        this.address = address;
        try {
            //TODO ids em bytes, nao em string
            this.id = Utility.sha256toString(Utility.sha256(this.address+":"+this.port));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getNodeId() {
        return id;
    }

    public Node requestFindSucc(Node node) {
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
            String request = "FINDSUCC " + node.getNodeId() + " " + id + " \n";
            out.println(request);
            String reply = in.readLine();

            //TODO see what to do with reply
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO missing returns
        return null;
    }

}
