import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class SSLConnection implements Runnable {

    private int port;
    private String ipAddress;
    private ObjectOutputStream dos;

    public SSLConnection(String ipAddress, int port) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public void send(Message msg) {
        //Create socket
        try {
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(this.ipAddress, this.port);

            dos = new ObjectOutputStream(sslSocket.getOutputStream());
            dos.flush();
            dos.writeObject(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sslSocket.close();

        } catch (IOException e) {
            if (msg.getHeader()[0].equals("CHECKPRED")) {
                Peer.predNode = null;
                System.out.println("Server - Failed to connect to predecessor");
            }
            else {
                Peer.unlockStabilize();
                System.out.println(Peer.latchStabilize.getCount());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {

        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket;

        try {

            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);

            while (true) {

                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                Peer.getThreadExecutor().execute(new ReceivedMessagesManager(clientSocket));
            }

        } catch (IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        }

    }
}
