import java.io.File;
import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Peer implements PeerInterface{

    private Integer peer_id;
    private Channel[] channels;
    private Storage storage;

    public Peer(Integer peer_id, Channel[] channels) {
        this.peer_id = peer_id;
        this.channels = channels;
        executeChannels();

        //I'm awake
        if (!PeerProtocol.getProtocol_version().equals("1.0"))
            awoke();
    }

    public void executeChannels() {
        //Initiate channels' threads
        for (int i = 0; i < 3; i++) {
            PeerProtocol.getThreadExecutor().execute(channels[i]);
        }
    }

    public void awoke() {
        MessageFactory messageFactory = new MessageFactory();
        byte msg[] = messageFactory.awakeMsg(this.peer_id);
        DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
        new Thread(new SendMessagesManager(sendPacket)).start();
    }

    public void initiateStorage() {
        this.storage = new Storage(peer_id);
    }

    public Channel getMCChannel(){
        return this.channels[0];
    }

    public Channel getMDBChannel() {
        return this.channels[1];
    }

    public Channel getMDRChannel() {
        return this.channels[2];
    }

    public Storage getStorage() {
        return this.storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Integer getPeer_id(){
        return this.peer_id;
    }

    @Override
    public synchronized String backup(String file_path, Integer replication_degree) {
        //File creation
        FileInfo file = new FileInfo(file_path);
        file.prepareChunks(replication_degree);

        //File store
        this.storage.storeFile(file, this.peer_id);

        //Send PUTCHUNK message for each file's chunk
        Iterator<Chunk> chunkIterator = file.getChunks().iterator();
        MessageFactory messageFactory = new MessageFactory();

        while(chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            byte msg[] = messageFactory.putChunkMsg(chunk, replication_degree, this.peer_id);
            DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
            new Thread(new SendMessagesManager(sendPacket)).start();
            String messageString = messageFactory.getMessageString();
            System.out.printf("Sent message: %s\n", messageString);
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            String chunkKey = chunk.getFile_id()+"-"+chunk.getChunk_no();
            PutChunkAttempts putChunkAttempts = new PutChunkAttempts(1, 5, sendPacket, chunkKey, replication_degree, messageString);
            PeerProtocol.getThreadExecutor().schedule(putChunkAttempts, 1, TimeUnit.SECONDS);
        }

        return "Backup successful";
    }

    @Override
    public synchronized String restore(String file) {
        boolean file_exists = false;
        Storage peerStorage = this.storage;
        for (int i = 0; i < peerStorage.getStoredFiles().size(); i++) {
            FileInfo fileInfo;
            if (peerStorage.getStoredFiles().get(i).getFile().getName().equals(file)) {
                file_exists = true;
                //Get previously backed up file
                fileInfo = peerStorage.getStoredFiles().get(i);
                //Get file chunks
                Set<Chunk> chunks = peerStorage.getStoredFiles().get(i).getChunks();
                Iterator<Chunk> chunkIterator = chunks.iterator();
                //For each chunk
                while (chunkIterator.hasNext()) {
                    Chunk chunk = chunkIterator.next();
                    //Prepare message to send
                    MessageFactory messageFactory = new MessageFactory();
                    byte[] msg = messageFactory.getChunkMsg(PeerProtocol.getProtocol_version(), this.peer_id, fileInfo.getFileId(), chunk.getChunk_no());
                    String messageString = messageFactory.getMessageString();

                    //Send message
                    DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
                    new Thread(new SendMessagesManager(sendPacket)).start();
                    System.out.printf("Sent message: %s\n", messageString);
                }
                while (fileInfo.getChunks().size() != this.storage.getRestoreChunks().size()) {}
                new Thread(new RestoreChunks(file)).start();
                break;
            }
        }

        if (file_exists)
            return "Restore successful";
        else return "File was not backed up previously";
    }

    @Override
    public synchronized String delete(String file_path) {
        boolean file_exists = false;
        FileInfo fileInfo;
        for (int i = 0; i < this.storage.getStoredFiles().size(); i++) {
            if (this.storage.getStoredFiles().get(i).getFile().getName().equals(file_path)) {
                file_exists = true;

                //Get previously backed up file
                fileInfo = this.storage.getStoredFiles().get(i);
                this.storage.getDeletedFiles().add(fileInfo);
                //Get file chunks
                Set<Chunk> chunks = fileInfo.getChunks();
                Iterator<Chunk> chunkIterator = chunks.iterator();
                //For each chunk
                while (chunkIterator.hasNext()) {
                    Chunk chunk = chunkIterator.next();
                    //Prepare message to send
                    MessageFactory messageFactory = new MessageFactory();
                    byte msg[] = messageFactory.deleteMsg(chunk, this.peer_id);
                    //Send message
                    DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
                    new Thread(new SendMessagesManager(sendPacket)).start();
                    String messageString = messageFactory.getMessageString();
                    System.out.printf("Sent message: %s\n", messageString);
                }
                //Delete file
                this.storage.deleteFile(fileInfo);

                break;
            }
        }

        if (file_exists)
            return "Delete successful";
        else return "File does not exist";
    }

    @Override
    public synchronized String reclaim(Integer max_space) {

        double spaceUsed = this.storage.getOccupiedSpace();
        double spaceClaimed = max_space; //The client shall specify the maximum disk space in KBytes (1KByte = 1000 bytes)

        double tmpSpace = spaceUsed - spaceClaimed;

        if (tmpSpace > 0) {
            double deletedSpace = 0;
            Iterator<Chunk> chunkIterator = this.storage.getStoredChunks().iterator();

            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                if (deletedSpace < tmpSpace || max_space == 0) {
                    deletedSpace += chunk.getChunk_size();

                    MessageFactory messageFactory = new MessageFactory();
                    byte msg[] = messageFactory.reclaimMsg(chunk, this.peer_id);
                    DatagramPacket sendPacket = new DatagramPacket(msg, msg.length);
                    new Thread(new SendMessagesManager(sendPacket)).start();
                    String messageString = messageFactory.getMessageString();
                    System.out.printf("Sent message: %s\n", messageString);

                    String filepath = this.storage.getDirectory().getPath() + "/file" + chunk.getFile_id() + "/chunk" + chunk.getChunk_no();
                    File file = new File(filepath);
                    file.delete();
                    String chunkKey = chunk.getFile_id() + "-" + chunk.getChunk_no();
                    this.storage.decrementChunkOccurences(chunkKey);

                    if (this.storage.getChunkCurrentDegree(chunkKey) < chunk.getDesired_replication_degree()) {
                        byte msg2[] = messageFactory.putChunkMsg(chunk, chunk.getDesired_replication_degree(), this.peer_id);
                        DatagramPacket sendPacket2 = new DatagramPacket(msg2, msg2.length);
                        Random random = new Random();
                        int random_value = random.nextInt(401);
                        PeerProtocol.getThreadExecutor().schedule(new SendMessagesManager(sendPacket2), random_value, TimeUnit.MILLISECONDS);
                        String messageString2 = messageFactory.getMessageString();
                        System.out.printf("Sent message: %s\n", messageString2);
                    }
                    chunkIterator.remove();
                }
                else {
                    break;
                }
            }

            this.storage.reclaimSpace(max_space);

        }

        return "Reclaim successful";
    }

    @Override
    public synchronized Storage state() {
        //For each file whose backup it has initiated
        System.out.println("-> For each file whose backup it has initiated:");
        for (int i = 0; i < this.storage.getStoredFiles().size(); i++) {
            FileInfo fileInfo = this.storage.getStoredFiles().get(i);

            //File pathname
            String filename = fileInfo.getFile().getPath();
            System.out.println("File pathname: "+filename);

            //File id
            String fileId = fileInfo.getFileId();
            System.out.println("\tFile id: "+fileId);

            //Replication degree
            int repDeg = fileInfo.getReplicationDegree();
            System.out.println("\tFile desired replication degree: "+repDeg);

            System.out.println("\t-> For each chunk of the file:");
            Iterator<Chunk> chunkIterator = fileInfo.getChunks().iterator();
            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();

                //Chunk id
                String chunk_id = chunk.getFile_id()+"-"+chunk.getChunk_no();
                System.out.println("\t  Chunk id: "+chunk_id);

                //Perceived replication degree
                int repDegree = this.storage.getChunkCurrentDegree(chunk_id);
                System.out.println("\t  Perceived replication degree: "+repDegree);

            }
        }

        //For each chunk it stores
        System.out.println("-> For each chunk it stores:");
        for (int i = 0; i < this.storage.getStoredChunks().size(); i++) {
            Chunk chunk = this.storage.getStoredChunks().get(i);

            //Chunk id
            String chunk_id = chunk.getFile_id()+"-"+chunk.getChunk_no();
            System.out.println("\tChunk id: "+chunk_id);

            //Size
            int size = chunk.getChunk_size();
            System.out.println("\tChunk size: "+size);

            //Perceived replication degree
            int repDeg = this.storage.getChunkCurrentDegree(chunk_id);
            System.out.println("\tPerceived replication degree: "+repDeg);
        }

        return this.storage;
    }
}
