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
            String request = bufferedReader.readLine();

            //parseMsg(request);

            System.out.println("Received Message: " + request);

            sslSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

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
                //manageFindSucc(address, port, requestId, idToFind);
                break;
            }
            case "SUCC": {
                //manageSucc();
            }
            default:
                break;
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
        System.out.printf("Received message: PUTCHUNK %s %d %d\n", fileId, chunkNo, repDeg);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedPutChunk receivedPutChunk = new ReceivedPutChunk(fileId, chunkNo, repDeg, body);
        Peer.getThreadExecutor().schedule(receivedPutChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageStored(String fileId, int chunkNo) {
        Storage peerStorage = Peer.getStorage();
        String chunkKey = fileId+"-"+chunkNo;
        peerStorage.incrementChunkOccurences(chunkKey);
        System.out.printf("Received message: STORED %s %d\n", fileId, chunkNo);
    }

    private void manageRemoved(String fileId, int chunkNo) {
        System.out.printf("Received message: REMOVED %s %d\n", fileId, chunkNo);
        String chunkKey = fileId +"-"+chunkNo;
        Storage peerStorage = Peer.getStorage();
        peerStorage.decrementChunkOccurences(chunkKey);
    }

    private void manageGetChunk(String fileId, int chunkNo) {
        System.out.printf("Received message: GETCHUNK %s %d\n", fileId, chunkNo);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedGetChunk receivedGetChunk = new ReceivedGetChunk(fileId, chunkNo);
        Peer.getThreadExecutor().schedule(receivedGetChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageChunk(String fileId, int chunkNo, byte[] body) {
        System.out.printf("Received message: CHUNK %s %d\n", fileId, chunkNo);
        String chunkKey = fileId+"-"+chunkNo;
        Peer.getStorage().getRestoreChunks().putIfAbsent(chunkKey, body);
    }

    private void manageDelete(String fileId) {
        System.out.printf("Received message: DELETE %s\n", fileId);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedDelete receivedDelete = new ReceivedDelete(fileId);
        Peer.getThreadExecutor().schedule(receivedDelete, random_value, TimeUnit.MILLISECONDS);
    }

    //////////////////////
    /// CHORD MESSAGES ///
    /////////////////////

    private void manageFindSucc(BigInteger msgId, String address, int port, BigInteger id) {
        System.out.printf("Received message: FINDSUCC " + msgId+" "+address+" "+ port + " "+id);

        ReceivedFindSucc receivedFindSucc = new ReceivedFindSucc(address, port, id);
        Peer.getThreadExecutor().execute(receivedFindSucc);
    }

    private void manageSucc(BigInteger msgId, BigInteger succId) {
        System.out.printf("Received message: SUCC " + msgId+" "+succId);

        /* SAMPLE
        //todo received find succ -  be careful how this is done because of the find fingers successor
        ReceivedFindSucc receivedFindSucc = new ReceivedFindSucc(msgId, address, port, id);
        Peer.getThreadExecutor().execute(receivedFindSucc);
        */

    }

}
