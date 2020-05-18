public class ReceivedTest implements Runnable {

    String address;
    int port;

    public ReceivedTest(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {

        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.replyTestMsg(Peer.getPeer().getNodeId());

        SendMessagesManager sendMessagesManager = new SendMessagesManager(message, this.address, this.port);

        Peer.getThreadExecutor().execute(sendMessagesManager);

    }
}
