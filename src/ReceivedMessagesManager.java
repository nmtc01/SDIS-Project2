import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivedMessagesManager implements Runnable {
    private String[] header;
    private byte[] body;

    public ReceivedMessagesManager(byte[] msg) {
        parseMsg(msg);
    }

    @Override
    public void run() {
        String version = header[0];
        String subProtocol = header[1];
        int senderId = Integer.parseInt(header[2]);
        String fileId = new String();
        if (header.length >= 4) {
            fileId = header[3];
        }
        int chunkNo = 0;
        if (header.length >= 5) {
            chunkNo = Integer.parseInt(header[4]);
        }
        int repDeg = 0;
        if (header.length == 6)
            repDeg = Integer.parseInt(header[5]);

        switch (subProtocol) {
            case "PUTCHUNK":
                managePutChunk(version, senderId, fileId, chunkNo, repDeg, body);
                break;
            case "STORED":
                manageStored(version, senderId, fileId, chunkNo);
                break;
            case "DELETE":
                manageDelete(version, senderId, fileId);
                break;
            case "GETCHUNK":
                manageGetChunk(version, senderId, fileId, chunkNo);
                break;
            case "CHUNK":
                manageChunk(version, senderId, fileId, chunkNo, body);
                break;
            case "CHUNKENH":
                manageChunkEnh(version, senderId, fileId, chunkNo);
                break;
            case "REMOVED":
                manageRemoved(version, senderId, fileId, chunkNo);
                break;
            case "AWAKE":
                manageAwake(version, senderId);
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

    private void managePutChunk(String version, int senderId, String fileId, int chunkNo, int repDeg, byte[] body) {
        //If the peer that sent is the same peer receiving
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s PUTCHUNK %d %s %d %d\n", version, senderId, fileId, chunkNo, repDeg);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedPutChunk receivedPutChunk = new ReceivedPutChunk(version, fileId, chunkNo, repDeg, body);
        PeerProtocol.getThreadExecutor().schedule(receivedPutChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageStored(String version, int senderId, String fileId, int chunkNo) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        Storage peerStorage = PeerProtocol.getPeer().getStorage();
        String chunkKey = fileId+"-"+chunkNo;
        peerStorage.incrementChunkOccurences(chunkKey);
        peerStorage.add_peer_chunks(chunkKey, senderId);
        System.out.printf("Received message: %s STORED %d %s %d\n", version, senderId, fileId, chunkNo);
    }

    private void manageRemoved(String version, int senderId, String fileId, int chunkNo) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s REMOVED %d %s %d\n", version, senderId, fileId, chunkNo);
        String chunkKey = fileId +"-"+chunkNo;
        Storage peerStorage = PeerProtocol.getPeer().getStorage();
        peerStorage.decrementChunkOccurences(chunkKey);
        peerStorage.remove_peer_chunks(chunkKey, senderId);
    }

    private void manageGetChunk(String version, int senderId, String fileId, int chunkNo) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s GETCHUNK %d %s %d\n", version, senderId, fileId, chunkNo);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedGetChunk receivedGetChunk = new ReceivedGetChunk(version, fileId, chunkNo);
        PeerProtocol.getThreadExecutor().schedule(receivedGetChunk, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageChunk(String version, int senderId, String fileId, int chunkNo, byte[] body) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        if (version.equals("1.0"))
            System.out.printf("Received message: %s CHUNK %d %s %d\n", version, senderId, fileId, chunkNo);
        else System.out.printf("Received enhanced message: %s CHUNK %d %s %d\n", version, senderId, fileId, chunkNo);
        String chunkKey = fileId+"-"+chunkNo;
        if (version.equals("1.0"))
            PeerProtocol.getPeer().getStorage().getRestoreChunks().putIfAbsent(chunkKey, body);
        else new Thread(new ReceiveRestoreEnh(fileId, chunkNo, new String(body), senderId)).start();
    }

    private void manageChunkEnh(String version, int senderId, String fileId, int chunkNo) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s CHUNK %d %s %d\n", version, senderId, fileId, chunkNo);
    }

    private void manageDelete(String version, int senderId, String fileId) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s DELETE %d %s\n", version, senderId, fileId);
        Random random = new Random();
        int random_value = random.nextInt(401);
        ReceivedDelete receivedDelete = new ReceivedDelete(version, fileId);
        PeerProtocol.getThreadExecutor().schedule(receivedDelete, random_value, TimeUnit.MILLISECONDS);
    }

    private void manageAwake(String version, int senderId) {
        if (senderId == PeerProtocol.getPeer().getPeer_id())
            return;
        System.out.printf("Received message: %s AWAKE %d\n", version, senderId);
        Storage peerStorage = PeerProtocol.getPeer().getStorage();
        ArrayList<FileInfo> deletedFiles = peerStorage.getDeletedFiles();

        if (!peerStorage.getPeers_with_chunks().containsKey(senderId))
            return;

        ArrayList<String> keys = peerStorage.getPeers_with_chunks().get(senderId);
        int deleted_size = deletedFiles.size();
        int keys_size = keys.size();
        for (int i = 0; i < deleted_size; i++) {
            for (int j = 0; j < keys_size; j++) {
                if (deletedFiles.get(i).getFileId().equals(keys.get(j).split("-")[0])){
                    peerStorage.getStoredFiles().add(deletedFiles.get(i));
                    PeerProtocol.getPeer().delete(deletedFiles.get(i).getFile().getName());
                    break;
                }
            }
        }
    }
}
