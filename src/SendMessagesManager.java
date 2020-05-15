import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class SendMessagesManager implements Runnable {
    private DatagramPacket packet;
    private SSLSocket sslSocket;
    private byte[] message;

    SendMessagesManager(DatagramPacket packet) {
        this.packet = packet;
    }

    public SendMessagesManager(SSLSocket sslSocket, byte[] message) {
        this.sslSocket = sslSocket;
        this.message = message;
    }

    @Override
    public void run() {
        byte[] message = parsePacket(this.packet);
        String[] p_str = parsePacketStr(this.packet);
        String subProtocol = p_str[1];

        switch (subProtocol) {
            case "PUTCHUNK":
                managePutChunk(message);
                break;
            case "STORED":
                manageStored(message);
                break;
            case "DELETE":
                manageDelete(message);
                break;
            case "GETCHUNK":
                manageGetChunk(message);
                break;
            case "CHUNK":
                manageChunk(message);
                break;
            case "REMOVED":
                manageRemoved(message);
                break;
            case "SUCC":
                manageSucc(this.message);
                break;
            default:
                break;
        }
    }

    private byte[] parsePacket(DatagramPacket packet) {
        return packet.getData();
    }

    private String[] parsePacketStr(DatagramPacket packet) {
        String p = new String(packet.getData());
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

    private void manageSucc(byte[] message) {
        try {
            OutputStream outputStream = sslSocket.getOutputStream();
            outputStream.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
