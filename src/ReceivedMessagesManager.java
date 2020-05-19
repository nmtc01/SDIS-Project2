import javax.net.ssl.SSLSocket;
import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
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
            /*
            DataInputStream dataInputStream = new DataInputStream(sslSocket.getInputStream());
            byte[] request = dataInputStream.readAllBytes();
            */

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.sslSocket.getInputStream()));

            //TODO read byte[] instead of String
            String request = bufferedReader.readLine().trim();
            String[] header = request.split(" ");

            System.out.println("##################################");
            System.out.println("##      Received message:       ##");
            System.out.println("##################################");

            //Manage subProtocol
            String subProtocol = header[0];

            switch (subProtocol) {
                case "PUTCHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    int repDeg = Integer.parseInt(header[3]);
                    managePutChunk(fileId, chunkNo, repDeg, body);
                    break;
                }
                case "STORED": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    manageStored(fileId, chunkNo);
                    break;
                }
                case "DELETE": {
                    String fileId = header[1];
                    manageDelete(fileId);
                    break;
                }
                case "GETCHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    manageGetChunk(fileId, chunkNo);
                    break;
                }
                case "CHUNK": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    manageChunk(fileId, chunkNo, body);
                    break;
                }
                case "REMOVED": {
                    String fileId = header[1];
                    int chunkNo = Integer.parseInt(header[2]);
                    manageRemoved(fileId, chunkNo);
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
                    manageNofity(msgId,address,port);
                    break;
                }
                case "TEST":{
                    BigInteger msgId = new BigInteger(header[1]);
                    String succAddress = header[2];
                    int succPort = Integer.parseInt(header[3]);
                    manageTest(msgId,succAddress,succPort);
                    break;
                } case "REPTEST":{
                    BigInteger msgId = new BigInteger(header[1]);
                    manageReplyTest(msgId);
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void parseMsg(byte[] data) {
        int index;
        for (index = 0; index < data.length; index++) {
            if (data[index] == 0xD && data[index+1] == 0xA && data[index+2] == 0xD && data[index+3] == 0xA)
                break;
        }
        String headerStr = new String(Arrays.copyOfRange(data, 0, index));
        this.header = headerStr.split(" ");
        this.body = Arrays.copyOfRange(data, index+4, data.length);
    }
    
    ////////////////////////////////
    /// BACKUP PROTOCOL MESSAGES ///
    ////////////////////////////////

    private void managePutChunk(String fileId, int chunkNo, int repDeg, byte[] body) {
        System.out.printf("PUTCHUNK %s %d %d\n", fileId, chunkNo, repDeg);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedPutChunk receivedPutChunk = new ReceivedPutChunk(fileId, chunkNo, repDeg, body);
        Peer.getThreadExecutor().schedule(receivedPutChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageStored(String fileId, int chunkNo) {
        System.out.printf("STORED %s %d\n", fileId, chunkNo);
        Storage peerStorage = Peer.getStorage();
        String chunkKey = fileId+"-"+chunkNo;
        peerStorage.incrementChunkOccurences(chunkKey);
    }

    private void manageRemoved(String fileId, int chunkNo) {
        System.out.printf("REMOVED %s %d\n", fileId, chunkNo);
        String chunkKey = fileId +"-"+chunkNo;
        Storage peerStorage = Peer.getStorage();
        peerStorage.decrementChunkOccurences(chunkKey);
        //TODO missing removing peer from peers_with_chunks on storage
    }

    private void manageGetChunk(String fileId, int chunkNo) {
        System.out.printf("GETCHUNK %s %d\n", fileId, chunkNo);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedGetChunk receivedGetChunk = new ReceivedGetChunk(fileId, chunkNo);
        Peer.getThreadExecutor().schedule(receivedGetChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageChunk(String fileId, int chunkNo, byte[] body) {
        System.out.printf("CHUNK %s %d\n", fileId, chunkNo);
        String chunkKey = fileId+"-"+chunkNo;
        Peer.getStorage().getRestoreChunks().putIfAbsent(chunkKey, body);
    }

    private void manageDelete(String fileId) {
        System.out.printf("DELETE %s\n", fileId);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedDelete receivedDelete = new ReceivedDelete(fileId);
        Peer.getThreadExecutor().schedule(receivedDelete, random_value, TimeUnit.MILLISECONDS);
    }

    //////////////////////
    /// CHORD MESSAGES ///
    /////////////////////

    private void manageFindSucc(BigInteger msgId, String address, int port, BigInteger id) {

        //System.out.println("Received message: FINDSUCC " + msgId+" "+address+" "+ port + " "+id);

        Node node = Peer.getPeer().findSucc(address, port, id);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort());

            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }
    }

    private void manageSucc(BigInteger msgId, BigInteger succId, String succAddress, int succPort) {

        //System.out.println("Received message: SUCC " + msgId+" "+succId + " "+succAddress+" "+succPort);

        Node succNode = new Node(succId, succAddress, succPort);
        Peer.setSuccNode(succNode);

    }

    private void manageFindSuccFinger(BigInteger msgId, String address, int port, BigInteger id, int fingerId) {

        //System.out.println("Received message: FINDSUCCFINGER " + msgId+" "+address + " "+port+" "+id+" "+fingerId);

        Node node = Peer.getPeer().findSuccFinger(address, port,id,fingerId);

        if (node != null) {

            MessageFactory messageFactory = new MessageFactory();
            byte[] message = messageFactory.replySuccFingerMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort(),fingerId);

            SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);

            Peer.getThreadExecutor().execute(sendMessagesManager);
        }


    }


    private void manageSuccFinger(BigInteger msgId, BigInteger succId, String succAddress, int succPort, int fingerId) {
        //System.out.println("Received message: FINGERSUCC " + msgId+" "+succId + " "+succAddress+" "+succPort+" "+fingerId);

        Node succNode = new Node(succId, succAddress, succPort);
        //this.succId = new Node(succAddress, succPort); //todo this should be the stuff

        Peer.updateFinger(succNode,fingerId);

    }


    private void manageNofity(BigInteger msgId, String address, int port) {
        //System.out.println("Received message: NOTIFY " + msgId+" "+address+" "+ port);

        //Node node = new Node(address, port); //Todo use this
        Node node = new Node(msgId,address,port);

        Peer.getPeer().notify(node);
    }

    private void manageTest(BigInteger msgId, String address, int port) {

       // System.out.println("Received message: TEST " + msgId+" "+address+" "+ port);

        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.replyTestMsg(Peer.getPeer().getNodeId());

        SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);

        Peer.getThreadExecutor().execute(sendMessagesManager);
    }

    private void manageReplyTest(BigInteger msgId) {

        System.out.println("Received message: REPTEST " + msgId);

        Peer.predActive = true;


    }

    private void manageFindPred(BigInteger msgId,String address, int port ) {
       // System.out.println("Received message: FINDPRED " + msgId +" "+ address +" "+port);

        Node node = Peer.findPred();

        MessageFactory messageFactory = new MessageFactory();
        byte[] message = messageFactory.predMsg(Peer.getPeer().getNodeId(),node.getNodeId(),node.getAddress(),node.getPort());

        SendMessagesManager sendMessagesManager = new SendMessagesManager(message, address, port);

        Peer.getThreadExecutor().execute(sendMessagesManager);
    }

    private void managePred(BigInteger msgId, BigInteger predId, String predAddress, int predPort) {
       // System.out.println("Received message: PRED " + msgId + " "+ predId+" "+ predAddress+" "+predPort );

        //Node node = new Node(predAddress,predPort); //todo change to this
        Node node = new Node(predId,predAddress,predPort);

        Peer.updateSetStabilizeX(node);
    }


}
