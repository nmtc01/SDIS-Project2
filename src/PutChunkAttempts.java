import java.net.DatagramPacket;
import java.util.concurrent.TimeUnit;

public class PutChunkAttempts implements Runnable {
    private int time;
    private int attempts;
    private int counter;
    private DatagramPacket message;
    private String chunkKey;
    private int desiredRepDeg;
    private String messageHeader;

    public PutChunkAttempts(int time, int attempts, DatagramPacket message, String chunkKey, int repDeg, String messageHeader) {
        this.time = time;
        this.attempts = attempts;
        this.message = message;
        this.chunkKey = chunkKey;
        this.desiredRepDeg = repDeg;
        this.counter = 1;
        this.messageHeader = messageHeader;
    }

    @Override
    public void run() {
        int currentRepDeg = PeerProtocol.getPeer().getStorage().getChunkCurrentDegree(this.chunkKey);

        if (!PeerProtocol.getProtocol_version().equals("1.0")) {
            if (currentRepDeg < this.desiredRepDeg && this.counter < this.attempts && !PeerProtocol.getPeer().getStorage().hasStored(this.chunkKey)) {
                new Thread(new SendMessagesManager(this.message)).start();
                System.out.printf("Sent message: %s\n", this.messageHeader);
                this.counter++;
                this.time = this.time * 2;
                PeerProtocol.getThreadExecutor().schedule(this, this.time, TimeUnit.SECONDS);
            }
        }
        else if (this.counter < this.attempts && !PeerProtocol.getPeer().getStorage().hasStored(this.chunkKey)) {
            new Thread(new SendMessagesManager(this.message)).start();
            System.out.printf("Sent message: %s\n", this.messageHeader);
            this.counter++;
            this.time = this.time * 2;
            PeerProtocol.getThreadExecutor().schedule(this, this.time, TimeUnit.SECONDS);
        }
    }
}
