import javax.net.ssl.SSLSocket;
import java.io.*;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedMessagesManager implements Runnable {

    private String[] header;
    private byte[] body;
    private SSLSocket sslSocket;

    public ReceivedMessagesManager(SSLSocket sslSocket) {

        this.sslSocket = sslSocket;
    }

    @Override
    public void run() {

        //Read from connection
        try {
            //Read object
            ObjectInputStream objectInputStream = new ObjectInputStream(sslSocket.getInputStream());
            Message message = (Message)objectInputStream.readObject();
            this.header = message.getHeader();
            this.body = message.getBody();

            //Manage subProtocol
            String subProtocol = header[0];

            switch (subProtocol) {
                case "PUTCHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    int repDeg = Integer.parseInt(header[3]);
                    String address = header[4];
                    int port = Integer.parseInt(header[5]);
                    managePutChunk(fileId, chunkNo, repDeg, body, address, port);
                    break;
                }
                case "STORED": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    String address = header[3];
                    int port = Integer.parseInt(header[4]);
                    manageStored(fileId, chunkNo, address, port);
                    break;
                }
                case "DELETE": {
                    String fileId = header[1];
                    String address = header[2];
                    int port = Integer.parseInt(header[3]);
                    manageDelete(fileId, address, port);
                    break;
                }
                case "GETCHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    String address = header[3];
                    int port = Integer.parseInt(header[4]);
                    manageGetChunk(fileId, chunkNo, address, port);
                    break;
                }
                case "CHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    String address = header[3];
                    int port = Integer.parseInt(header[4]);
                    manageChunk(fileId, chunkNo, body, address, port);
                    break;
                }
                case "REMOVED": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    String address = header[3];
                    int port = Integer.parseInt(header[4]);
                    manageRemoved(fileId, chunkNo, address, port);
                    break;
                }
                case "FINDSUCC": {
                    BigInteger msgId = new BigInteger(header[1]);
                    String address = header[2];
                    int port = Integer.parseInt(header[3]);
                    BigInteger id = new BigInteger(header[4]);
                    manageFindSucc(msgId, address, port,id);
                    break;
                }
                case "SUCC": {
                    BigInteger msgId = new BigInteger(header[1]);
                    BigInteger succId = new BigInteger(header[2]);
                    String succAddress = header[3];
                    int succPort = Integer.parseInt(header[4]);
                    manageSucc(msgId,succId,succAddress,succPort);
                    break;
                }
                case "FINDSUCCFINGER": {
                    BigInteger msgId = new BigInteger(header[1]);
                    String address = header[2];
                    int port = Integer.parseInt(header[3]);
                    BigInteger id = new BigInteger(header[4]);
                    int fingerId = Integer.parseInt(header[5]);
                    manageFindSuccFinger(msgId, address, port,id,fingerId);
                    break;
                }
                case "FINGERSUCC": {
                    BigInteger msgId = new BigInteger(header[1]);
                    BigInteger succId = new BigInteger(header[2]);
                    String succAddress = header[3];
                    int succPort = Integer.parseInt(header[4]);
                    int fingerId = Integer.parseInt(header[5]);
                    manageSuccFinger(msgId,succId,succAddress,succPort,fingerId);
                    break;
                }
                case "NOTIFY":{
                    BigInteger msgId = new BigInteger(header[1]);
                    String address = header[2];
                    int port = Integer.parseInt(header[3]);
                    manageNotify(msgId,address,port);
                    break;
                }
                case "FINDPRED":{
                    BigInteger msgId = new BigInteger(header[1]);
                    String address = header[2];
                    int port = Integer.parseInt(header[3]);
                    manageFindPred(msgId,address,port);
                    break;
                }
                case "PRED":{
                    BigInteger msgId = new BigInteger(header[1]);
                    BigInteger predId = new BigInteger(header[2]);
                    String predAddress = header[3];
                    int predPort = Integer.parseInt(header[4]);
                    managePred(msgId,predId,predAddress,predPort);
                    break;
                }
                default:
                    break;
            }

            sslSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    ////////////////////////////////
    /// BACKUP PROTOCOL MESSAGES ///
    ////////////////////////////////

    private void managePutChunk(String fileId, int chunkNo, int repDeg, byte[] body, String senderAddress, int senderPort) {
        System.out.printf("Received message: PUTCHUNK %s %d %d %s %d\n", fileId, chunkNo, repDeg, senderAddress, senderPort);
        Random random = new Random();
        int random_value = random.nextInt(401);
        if (!(Peer.getPeer().getAddress().equals(senderAddress) && Peer.getPeer().getPort() == senderPort)) {
            ReceivedPutChunk receivedPutChunk = new ReceivedPutChunk(fileId, chunkNo, repDeg, body, senderAddress, senderPort);
            Peer.getThreadExecutor().schedule(receivedPutChunk, random_value, TimeUnit.MILLISECONDS);
        }
    }

    private void manageStored(String fileId, int chunkNo, String senderAddress, int senderPort) {
        System.out.printf("Received message: STORED %s %d %s %d\n", fileId, chunkNo, senderAddress, senderPort);
        Storage peerStorage = Peer.getStorage();
        String chunkKey = fileId+"-"+chunkNo;
        peerStorage.incrementChunkOccurences(chunkKey);
        peerStorage.add_peer_chunks(chunkKey, senderAddress, senderPort);
    }

    private void manageRemoved(String fileId, int chunkNo, String senderAddress, int senderPort) {
        System.out.printf("Received message: REMOVED %s %d %s %d\n", fileId, chunkNo, senderAddress, senderPort);
        String chunkKey = fileId +"-"+chunkNo;
        Storage peerStorage = Peer.getStorage();
        peerStorage.decrementChunkOccurences(chunkKey);
        peerStorage.remove_peer_chunks(chunkKey, senderAddress, senderPort);

        FileInfo file = null;
        for (FileInfo fileInfo: peerStorage.getStoredFiles()) {
            if (fileInfo.getFileId().equals(fileId)) {
                file = fileInfo;
            }
        }
        if (file == null)
            return;
        System.out.println("current: " + peerStorage.getChunkCurrentDegree(chunkKey) + "  initial: " + file.getReplicationDegree());
        if (peerStorage.getChunkCurrentDegree(chunkKey) < file.getReplicationDegree()) {
            // Get chunk from storage
            Chunk chunk = null;
            for (Chunk fileChunk: file.getChunks()) {
                if (fileChunk.getChunk_no() == chunkNo){
                    chunk = fileChunk;
                }
            }
            if (chunk == null)
                return;

            MessageFactory messageFactory = new MessageFactory();
            Message msg = messageFactory.putChunkMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), chunk, peerStorage.getChunkCurrentDegree(chunkKey));

            for (int i = 0; i < file.getReplicationDegree() - peerStorage.getChunkCurrentDegree(chunkKey); i++) {
                Peer.getThreadExecutor().execute(new SendMessagesManager(msg, Peer.succNode.getAddress(), Peer.succNode.getPort()));
                msg.printSentMessage();
            }
        }
    }

    private void manageGetChunk(String fileId, int chunkNo, String senderAddress, int senderPort) {
        System.out.printf("Received message: GETCHUNK %s %d %s %d\n", fileId, chunkNo, senderAddress, senderPort);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedGetChunk receivedGetChunk = new ReceivedGetChunk(fileId, chunkNo, senderAddress, senderPort);
        Peer.getThreadExecutor().schedule(receivedGetChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageChunk(String fileId, int chunkNo, byte[] body, String senderAddress, int senderPort) {
        System.out.printf("Received message: CHUNK %s %d %s %d\n", fileId, chunkNo, senderAddress, senderPort);
        String chunkKey = fileId+"-"+chunkNo;
        Peer.getStorage().getRestoreChunks().putIfAbsent(chunkKey, body);
    }

    private void manageDelete(String fileId, String senderAddress, int senderPort) {
        System.out.printf("Received message: DELETE %s %s %d\n", fileId, senderAddress, senderPort);
        Storage peerStorage = Peer.getStorage();
        peerStorage.deleteChunk(fileId);
    }

    //////////////////////
    /// CHORD MESSAGES ///
    /////////////////////

    private void manageFindSucc(BigInteger msgId, String address, int port, BigInteger id) {

        //System.out.println("Received message: FINDSUCC " + msgId+" "+address+" "+ port + " "+id);

        Node node = Peer.getPeer().findSucc(address, port, id);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            Message message = messageFactory.replySuccMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort());
            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);
            Peer.getThreadExecutor().execute(sendMessagesManager);
        }
    }

    private void manageSucc(BigInteger msgId, BigInteger succId, String succAddress, int succPort) {

        //System.out.println("Received message: SUCC " + msgId+" "+succId + " "+succAddress+" "+succPort);

        //Node succNode = new Node(succId, succAddress, succPort); //TO DEBUG USE THIS
        Node succNode = new Node(succAddress, succPort);

        Peer.setSuccNode(succNode);

    }

    private void manageFindSuccFinger(BigInteger msgId, String address, int port, BigInteger id, int fingerId) {

        //System.out.println("Received message: FINDSUCCFINGER " + msgId+" "+address + " "+port+" "+id+" "+fingerId);

        Node node = Peer.getPeer().findSuccFinger(address, port,id,fingerId);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            Message message = messageFactory.replySuccFingerMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort(),fingerId);
            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);
            Peer.getThreadExecutor().execute(sendMessagesManager);
        }


    }


    private void manageSuccFinger(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {
        //System.out.println("Received message: FINGERSUCC " + msgId+" "+succId + " "+succAddress+" "+succPort+" "+fingerId);

        //Node succNode = new Node(succId, succAddress, succPort);//TO DEBUG USE THIS
        Node succNode = new Node(succAddress, succPort);

        Peer.updateFinger(succNode,fingerId);

    }


    private void manageNotify(BigInteger msgId, String address, int port) {
        //System.out.println("Received message: NOTIFY " + msgId+" "+address+" "+ port);

        //Node node = new Node(msgId,address,port); //TO DEBUG USE THIS
        Node node = new Node(address, port);

        Peer.getPeer().notify(node);
    }

    private void manageFindPred(BigInteger msgId,String address, int port ) {
        //System.out.println("Received message: FINDPRED " + msgId +" "+ address +" "+port);

        Node node = Peer.findPred();

        MessageFactory messageFactory = new MessageFactory();

        try {
            Message message = messageFactory.predMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort());

            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);
            Peer.getThreadExecutor().execute(sendMessagesManager);
        } catch (Exception e) {
            System.out.println("Error - Predecessor is null");
        }
    }

    private void managePred(BigInteger msgId, BigInteger predId, String predAddress, int predPort) {
        //System.out.println("Received message: PRED " + msgId + " "+ predId+" "+ predAddress+" "+predPort );

        //Node node = new Node(predId,predAddress,predPort); //TO DEBUG USE THIS
        Node node = new Node(predAddress,predPort);

        Peer.updateSetStabilizeX(node);
    }


}
