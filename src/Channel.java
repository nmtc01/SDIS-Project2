import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class Channel implements Runnable {
    private String address;
    private Integer port;
    private InetAddress group;
    private MulticastSocket receiverSocket;

    public Channel(String address, String port) throws IOException {
        this.address = address;
        this.port = Integer.parseInt(port);
        this.group = InetAddress.getByName(this.address);
        this.receiverSocket = new MulticastSocket(this.port);
        this.receiverSocket.joinGroup(this.group);
    }

    public void send(byte[] msg) {
        try {
            DatagramPacket packet = new DatagramPacket(msg, msg.length, this.group, this.port);
            receiverSocket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            byte[] buf = new byte[65000];

            while (true) {
                //REQUEST
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                receiverSocket.receive(packet);
                //MESSAGE
                byte[] msg = Arrays.copyOf(buf, packet.getLength());
                ReceivedMessagesManager manager = new ReceivedMessagesManager(msg);
                new Thread(manager).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
