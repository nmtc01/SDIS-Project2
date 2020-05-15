import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.util.concurrent.TimeUnit;


public class SSLConnection implements Runnable {
    Peer peer;

    SSLConnection(Peer peer) {
        this.peer = peer;
    }

    @Override
    public void run() {
        ServerSocketFactory sslServerSocketFactory;
        sslServerSocketFactory = SSLServerSocketFactory.getDefault();

        while (true) {

            Peer.getThreadExecutor().schedule(new ReceivedMessagesManager("".getBytes()), 0, TimeUnit.SECONDS);
        }
    }
}
