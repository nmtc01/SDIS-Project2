import javax.net.ssl.SSLSocket;

public class ReceivedFindSucc implements Runnable {

    ReceivedFindSucc() {
    }

    @Override
    public void run() {
        Node node = null; //Peer.getPeer().findSucc();
        if (node != null) {
            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccMsg();
            SendMessagesManager sendMessagesManager = new SendMessagesManager(message);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }


    }
}
