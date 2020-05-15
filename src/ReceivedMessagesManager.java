import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedMessagesManager implements Runnable {
    private String[] header;
    private byte[] body;
    private SSLSocket sslSocket;

    public ReceivedMessagesManager(SSLSocket sslSocket) {
        this.sslSocket = sslSocket;
        //parseMsg(msg);
    }

    @Override
    public void run() {
        // read from connection
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String request = bufferedReader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }



        String subProtocol = header[0];
        int senderId = Integer.parseInt(header[1]);
        String fileId = new String();
        if (header.length >= 3) {
            fileId = header[2];
        }
        int chunkNo = 0;
        if (header.length >= 4) {
            chunkNo = Integer.parseInt(header[3]);
        }
        int repDeg = 0;
        if (header.length == 5)
            repDeg = Integer.parseInt(header[4]);

        switch (subProtocol) {
            case "PUTCHUNK":
                managePutChunk(senderId, fileId, chunkNo, repDeg, body);
                break;
            case "STORED":
                manageStored(senderId, fileId, chunkNo);
                break;
            case "DELETE":
                manageDelete(senderId, fileId);
                break;
            case "GETCHUNK":
                manageGetChunk(senderId, fileId, chunkNo);
                break;
            case "CHUNK":
                manageChunk(senderId, fileId, chunkNo, body);
                break;
            case "REMOVED":
                manageRemoved(senderId, fileId, chunkNo);
                break;
            case "FINDSUCC":
                manageFindSucc();
                break;
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

    private void managePutChunk(int senderId, String fileId, int chunkNo, int repDeg, byte[] body) {
        //If the peer that sent is the same peer receiving
        if (senderId == Peer.getPeer_id())
            return;
        System.out.printf("Received message: PUTCHUNK %d %s %d %d\n", senderId, fileId, chunkNo, repDeg);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedPutChunk receivedPutChunk = new ReceivedPutChunk(fileId, chunkNo, repDeg, body);
        Peer.getThreadExecutor().schedule(receivedPutChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageStored(int senderId, String fileId, int chunkNo) {
        if (senderId == Peer.getPeer_id())
            return;
        Storage peerStorage = Peer.getStorage();
        String chunkKey = fileId+"-"+chunkNo;
        peerStorage.incrementChunkOccurences(chunkKey);
        peerStorage.add_peer_chunks(chunkKey, senderId);
        System.out.printf("Received message: STORED %d %s %d\n", senderId, fileId, chunkNo);
    }

    private void manageRemoved(int senderId, String fileId, int chunkNo) {
        if (senderId == Peer.getPeer_id())
            return;
        System.out.printf("Received message: REMOVED %d %s %d\n", senderId, fileId, chunkNo);
        String chunkKey = fileId +"-"+chunkNo;
        Storage peerStorage = Peer.getStorage();
        peerStorage.decrementChunkOccurences(chunkKey);
        peerStorage.remove_peer_chunks(chunkKey, senderId);
    }

    private void manageGetChunk(int senderId, String fileId, int chunkNo) {
        if (senderId == Peer.getPeer_id())
            return;
        System.out.printf("Received message: GETCHUNK %d %s %d\n", senderId, fileId, chunkNo);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedGetChunk receivedGetChunk = new ReceivedGetChunk(fileId, chunkNo);
        Peer.getThreadExecutor().schedule(receivedGetChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageChunk(int senderId, String fileId, int chunkNo, byte[] body) {
        if (senderId == Peer.getPeer_id())
            return;
        System.out.printf("Received message: CHUNK %d %s %d\n", senderId, fileId, chunkNo);
        String chunkKey = fileId+"-"+chunkNo;
        Peer.getStorage().getRestoreChunks().putIfAbsent(chunkKey, body);
    }

    private void manageDelete(int senderId, String fileId) {
        if (senderId == Peer.getPeer_id())
            return;
        System.out.printf("Received message: DELETE %d %s\n", senderId, fileId);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedDelete receivedDelete = new ReceivedDelete(fileId);
        Peer.getThreadExecutor().schedule(receivedDelete, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageFindSucc() {
        //Peer.getPeer().findSucc();
        System.out.printf("Received message: FINDSUCC %d %s\n");

        ReceivedFindSucc receivedFindSucc = new ReceivedFindSucc(sslSocket);
        Peer.getThreadExecutor().execute(receivedFindSucc);
    }
}
