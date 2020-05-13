import java.io.UnsupportedEncodingException;
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

    //TODO verify
    public Node findSucc(Peer peer) {
        if (peer.getNodeId().equals(this.id)) {
            //TODO not this?
            return this;
        }
        else {
            Node newNode = closestPrecedNode(peer.getNodeId());
            return newNode.findSucc(peer);
        }
    }

    public Node closestPrecedNode(String id) {
        //TODO
        return null;
    }
}
