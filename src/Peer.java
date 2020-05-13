import java.io.*;
import java.net.DatagramPacket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer implements PeerInterface{
    //Args
    private static Integer peer_id;
    private static Storage storage;
    private static ArrayList<Node> fingerTable = new ArrayList<>();
    private static String acc_point;
    private static String initAddress;
    private static int initPort;
    private static ScheduledThreadPoolExecutor threadExecutor; //TODO use this instead of thread

    public Peer(Integer id) {
        peer_id = id;
    }

    public static void main(String args[]) {
        System.out.println("Starting Peer Protocol");
        //Parse args
        if (!parseArgs(args))
            return;

        //Create executor
        threadExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

        //Create initiator peer
        Peer peer = new Peer(peer_id);
        System.out.println("Started peer with id " + peer_id);

        //Establish RMI communication between TestApp and Peer
        establishRMICommunication(peer);

        //Initiate or load storage for initiator peer
        if (!loadPeerStorage())
            peer.initiateStorage();

        //Safe and exit
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::savePeerStorage));
    }

    public static boolean parseArgs(String[] args) {
        //Check the number of arguments given
        if (args.length != 4 && args.length != 6) {
            System.out.println("Usage: java Peer peer_id access_point addressInit portInit [address port]");
            return false;
        }

        //Parse peer id
        peer_id = Integer.parseInt(args[0]);
        //Parse access point
        acc_point = args[1];
        //Parse initAddress
        initAddress = args[2];
        //Parse initPort
        initPort = Integer.parseInt(args[3]);

        return true;
    }

    public static void establishRMICommunication(Peer peer) {
        try {
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(acc_point, stub);

        } catch (Exception e) {
            System.err.println("Peer Protocol exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static ScheduledThreadPoolExecutor getThreadExecutor() {
        return threadExecutor;
    }

    private static void savePeerStorage() {
        try {
            Storage storage = Peer.getStorage();
            String filename;
            if (storage.isUnix())
                filename = "Storage/" + peer_id + "/storage.ser";
            else filename = "Storage\\" + peer_id + "\\storage.ser";

            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(storage);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private static boolean loadPeerStorage() {
        try {
            String filenameUnix = "Storage/" + peer_id + "/storage.ser";
            String filenameWin = "Storage\\" + peer_id + "\\storage.ser";

            File file = new File(filenameUnix);
            String filename = filenameUnix;

            if (!file.exists()) {
                file = new File(filenameWin);
                filename = filenameWin;
                if (!file.exists()) {
                    return false;
                }
            }

            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            storage = (Storage) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return false;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            return false;
        }
        return true;
    }

    public static void initiateStorage() {
        storage = new Storage(peer_id);
    }

    public static Storage getStorage() {
        return storage;
    }

    public static int getPeer_id() {
        return peer_id;
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
            Peer.getThreadExecutor().schedule(putChunkAttempts, 1, TimeUnit.SECONDS);
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
                    byte[] msg = messageFactory.getChunkMsg(this.peer_id, fileInfo.getFileId(), chunk.getChunk_no());
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
                        Peer.getThreadExecutor().schedule(new SendMessagesManager(sendPacket2), random_value, TimeUnit.MILLISECONDS);
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

    @Override
    public synchronized String debug() {
        //TODO communication TCP

        return "Debug successfull";
    }
}
