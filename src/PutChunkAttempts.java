import java.util.concurrent.TimeUnit;

public class PutChunkAttempts implements Runnable {
    private int time;
    private int attempts;
    private int counter;
    private Message message;
    private String chunkKey;
    private int desiredRepDeg;
    private Node dest;

    public PutChunkAttempts(int time, int attempts, Message message, String chunkKey, int repDeg, Node dest) {
        this.time = time;
        this.attempts = attempts;
        this.message = message;
        this.chunkKey = chunkKey;
        this.desiredRepDeg = repDeg;
        this.counter = 1;
        this.dest = dest;
    }

    @Override
    public void run() {
        int currentRepDeg = Peer.getStorage().getChunkCurrentDegree(this.chunkKey);

        if (currentRepDeg < this.desiredRepDeg && this.counter < this.attempts) {
            Peer.getThreadExecutor().execute(new SendMessagesManager(this.message, this.dest.getAddress(), this.dest.getPort()));
            this.message.printSentMessage();
            this.counter++;
            this.time = this.time * 2;
            Peer.getThreadExecutor().schedule(this, this.time, TimeUnit.SECONDS);
        }
    }
}
