import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

public class SendMessagesManager implements Runnable {
    private byte[] message;

    public SendMessagesManager(byte[] message) {
        this.message = message;
    }

    @Override
    public void run() {
        String[] p_str = parseMsgToString(this.message);
        String subProtocol = p_str[1];

        switch (subProtocol) {
            case "PUTCHUNK":
                managePutChunk(this.message);
                break;
            case "STORED":
                manageStored(this.message);
                break;
            case "DELETE":
                manageDelete(this.message);
                break;
            case "GETCHUNK":
                manageGetChunk(this.message);
                break;
            case "CHUNK":
                manageChunk(this.message);
                break;
            case "REMOVED":
                manageRemoved(this.message);
                break;
            case "FINDSUCC":
                manageFindSucc(this.message);
                break;
            case "SUCC":
                manageSucc(this.message);
                break;
            default:
                break;
        }
    }

    private String[] parseMsgToString(byte[] request) {
        String p = new String(request);
        String[] pArray = p.trim().split(" ");
        return pArray;
    }

    private void managePutChunk(byte[] message) {
        //PeerProtocol.getPeer().getMDBChannel().send(message);
    }

    private void manageStored(byte[] message) {
        //PeerProtocol.getPeer().getMCChannel().send(message);
    }

    private void manageRemoved(byte[] message) {
        //PeerProtocol.getPeer().getMCChannel().send(message);
    }

    private void manageChunk(byte[] message) {
        //PeerProtocol.getPeer().getMDRChannel().send(message);
    }

    private void manageGetChunk(byte[] message) {
        //PeerProtocol.getPeer().getMCChannel().send(message);
    }

    private void manageDelete(byte[] message) {
        //PeerProtocol.getPeer().getMCChannel().send(message);
    }

    private void manageFindSucc(byte[] message) {

    }

    private void manageSucc(byte[] message) {

        //InetAddress host_name = InetAddress.getByName(address);
        /*SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();

        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
