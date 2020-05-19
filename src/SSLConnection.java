import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class SSLConnection implements Runnable {

    private int port;
    private String ipAddress;
    private DataOutputStream dos;

    public SSLConnection(String ipAddress, int port) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public void send(byte[] msg) {
        //Create socket
        //TODO check this
        try {
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(this.ipAddress, this.port);

            dos = new DataOutputStream(sslSocket.getOutputStream());

            System.out.println("##################################");
            System.out.println("##       Sending message:       ##");
            System.out.println("##################################");
            System.out.println((new String(msg, StandardCharsets.UTF_8)).trim() + " to "+this.ipAddress+":"+this.port);
            // BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
           // System.out.println("Sending message: "+ new String(msg, StandardCharsets.UTF_8) + " to "+this.ipAddress+":"+this.port);

            dos.flush();
            dos.write(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //String reply = in.readLine();

            sslSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
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
