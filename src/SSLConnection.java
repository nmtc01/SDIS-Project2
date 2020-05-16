import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;


public class SSLConnection implements Runnable {
    private int port;
    private String ipAddress;

    public SSLConnection(int port, String ipAddress) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public void send(byte[] msg) {
        //Create socket
        //TODO check this
        try {
            InetAddress host_name = InetAddress.getByName(this.ipAddress);
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host_name, this.port);

            /*if (cypher_suite.length > 0) {
                sslSocket.setEnabledCipherSuites(cypher_suite);
            }*/

            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            // BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

            out.flush();
            out.println(msg);

            //String reply = in.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket;
        try {
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port); // TODO change port

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                Peer.getThreadExecutor().execute(new ReceivedMessagesManager(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
