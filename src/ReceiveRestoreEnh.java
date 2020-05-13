import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

public class ReceiveRestoreEnh implements Runnable {
    private String fileId;
    private int chunkNo;
    private String address;
    private int port;
    private Socket echoSocket;

    public ReceiveRestoreEnh(String fileId, int chunkNo, String address, int sender_id) {;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.address = address;
        this.port = 4444+sender_id+chunkNo+Integer.valueOf(fileId.charAt(0));
        try {
            InetAddress host_name = InetAddress.getByName(this.address);
            this.echoSocket = new Socket(host_name, this.port);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(this.echoSocket.getInputStream());
            byte[] body = in.readAllBytes();
            PeerProtocol.getPeer().getStorage().getRestoreChunks().putIfAbsent(this.fileId+"-"+this.chunkNo, body);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
