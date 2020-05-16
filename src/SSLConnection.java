import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;


public class SSLConnection implements Runnable {

    Peer peer;

    SSLConnection(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket;

        try {

            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.peer.getPort());

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
