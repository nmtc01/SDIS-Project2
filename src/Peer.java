import java.io.*;
import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer extends Node implements PeerInterface, java.io.Serializable{

    private static int m = 160;
    private static int delay = 5;

    //Args
    private static Peer peer;
    private static Storage storage;
    private static String acc_point;
    private static String ipAddress;
    private static int port;
    private static String initIpAddress;
    private static int initPort;
    private static Node[] fingerTable = new Node[m];
    private static ScheduledThreadPoolExecutor threadExecutor;

    public static Node predNode;
    public static Node succNode;

    private static Node stabilizeX;

    //lock joinning thread
    public static final CountDownLatch latchJoin = new CountDownLatch(1);
    public static  CountDownLatch latchStabilize = new CountDownLatch(1);

    public Peer(String ipAddress, int port) {

        //create a new chord ring
        super(ipAddress, port);
        succNode = this;

        System.out.println("\nCreated with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Init Peer - "+this.getAddress()+":"+this.getPort());

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), delay, delay, TimeUnit.SECONDS);
    }

    public Peer(String ipAddress, int port, String initAddress, int initPort) {

        super(ipAddress, port);
        succNode = this;

        System.out.println("\nCreated with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Peer - "+this.getAddress()+":"+this.getPort());

        this.join(new Node(initAddress, initPort));

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), delay, delay, TimeUnit.SECONDS);
    }

    //TO DEBUG USE THIS
    public Peer(int order, String ipAddress, int port) {

        //create a new chord ring
        super(order, ipAddress, port);
        succNode = this;

        System.out.println("\nCreated with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Init Peer - "+this.getAddress()+":"+this.getPort());

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), delay, delay, TimeUnit.SECONDS);
    }

    //TO DEBUG USE THIS
    public Peer(int order, String ipAddress, int port, String initAddress, int initPort) {

        super(order, ipAddress, port);
        succNode = this;

        System.out.println("\nCreated with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Peer - "+this.getAddress()+":"+this.getPort());

        this.join(new Node(initAddress, initPort));

        this.printFingerTable();

         threadExecutor.scheduleAtFixedRate( new ChordManager(this), delay, delay, TimeUnit.SECONDS);
    }

    public static void setSuccNode(Node sn){
        succNode = sn;
        fingerTable[0] = sn;
        System.out.println("\n Setup Succ "+sn.getNodeId() );
        latchJoin.countDown();
    }

    public static void main(String args[]) {

        System.out.println("##################################");
        System.out.println("##    Starting Peer Protocol    ##");
        System.out.println("##################################");

        //Parse args
        if (!parseArgs(args))
            return;

        //Create executor
        threadExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(200);
        threadExecutor.execute(new SSLConnection(ipAddress,port));

        //Create initiator peer

        if (args.length == 5) {
            peer = new Peer(ipAddress, port, initIpAddress, initPort);
        }
        else {
            peer = new Peer(ipAddress, port);
        }

        /*
        //DEBUG WITH M=4
        if (args.length == 6) {
            peer = new Peer(Integer.parseInt(args[5]),ipAddress, port, initIpAddress, initPort);
        }
        else {
            peer = new Peer(Integer.parseInt(args[3]),ipAddress, port);
        }

        */
        System.out.println("Started peer");

        //Establish RMI communication between TestApp and Peer
        establishRMICommunication(peer);

        //Initiate or load storage for initiator peer
        if (!loadPeerStorage()) {
            initiateStorage();
        }

        //Safe and exit
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::savePeerStorage));
    }

    public static boolean parseArgs(String[] args) {
        //Check the number of arguments given
        if (args.length != 3 && args.length != 5) { //TO DEBUG USE 4 - 6
            System.out.println("Usage: java Peer access_point addressInit portInit [address port]");
            return false;
        }

        //Parse access point
        acc_point = args[0];
        //Parse address
        ipAddress = args[1];
        //Parse port
        port = Integer.parseInt(args[2]);
        //Parse init
        if (args.length == 5) { //TO DEBUG USE 6
            initIpAddress = args[3];
            initPort = Integer.parseInt(args[4]);
        }

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

    public Node[] getFingerTable() {
        return fingerTable;
    }

    /////////////////////////////////
    //////// STORAGE SECTION ////////
    /////////////////////////////////

    private static void savePeerStorage() {
        try {
            Storage storage = Peer.getStorage();
            String filename = "Storage/"+Peer.getPeer().getNodeId()+"storage.ser";

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
            String filenameUnix = "Storage/"+peer.getNodeId()+"/storage.ser";

            File file = new File(filenameUnix);
            String filename = filenameUnix;

            if (!file.exists()) {
                return false;
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
        storage = new Storage();
    }

    public static Peer getPeer() {
        return peer;
    }

    public static Storage getStorage() {
        return storage;
    }

    /////////////////////////////////////////
    ////////  CHORD JOINING SECTION  ////////
    /////////////////////////////////////////

    /**¢
     * JOIN - join chord using ring containing node n'
     */
    public void join(Node initNode) {

        succNode = initNode.requestFindSucc(this.getNodeId(), this.getAddress(),this.getPort(), this.getNodeId());

        //open thread to wait for the response

        //System.out.println("JOIN - locking...");
        try {
            latchJoin.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("JOIN - joined - SUCC - "+succNode.getNodeId());

        predNode = succNode;

        System.out.println(this.getNodeId()+": "+getFinger(0));
        for (int i = 0; i < fingerTable.length; i++) {
            if (fallsBetween(getFinger(i), this.getNodeId(), succNode.getNodeId()))
                fingerTable[i] = succNode;
            else
                fingerTable[i] = this;
        }

    }

    public boolean fallsBetween(BigInteger id, BigInteger n, BigInteger succ){

        if (n.compareTo(succ) > 0)
            return (id.compareTo(n) > 0) || (id.compareTo(succ) <= 0);    // 5.compareTo(4) => 1

        return (id.compareTo(n) > 0) && (id.compareTo(succ) <= 0);

        //TO DEBUG USE THIS
        /*
        if (n.compareTo(succ) > 0)
            return (id.compareTo(n) > 0) || (id.compareTo(succ) <= 0);    // 5.compareTo(4) => 1

        return (id.compareTo(n) > 0) && (id.compareTo(succ) <= 0);
        */

    }

    public Node findSucc(String address, int port, BigInteger id){

        //case its the same id return itself

        if(succNode.getNodeId().equals(this.getNodeId())){
            if(!id.equals(this.getNodeId())){
                succNode = new Node(address,port);
                //succNode = new Node(id,address,port); //TO DEBUG USE THIS
                predNode = new Node(address,port);
                //predNode = new Node(id,address,port); //TO DEBUG USE THIS
                fingerTable[0] = succNode;
            }

            return this;
        }

        if( fallsBetween(id,this.getNodeId(),succNode.getNodeId())){
            return succNode;
        }else{
            Node newNode = closestPrecedNode(id);

            if(newNode.getNodeId().equals(this.getNodeId()))
                return this;

            return newNode.requestFindSucc(this.getNodeId(),address, port,id);
        }

    }

    public Node findSuccFinger(String address, int port, BigInteger id, int fingerId){
        //case its the same id return itself

        if( fallsBetween(id,this.getNodeId(),succNode.getNodeId())){
            return succNode;
        }else{
            Node newNode = closestPrecedNode(id);

            if(newNode.getNodeId().equals(this.getNodeId()))
                return this;

           return newNode.requestFindSuccFinger(this.getNodeId(),address, port,id,fingerId);
        }

    }

    public static Node findPred(){
        return predNode;
    }

    public Node closestPrecedNode(BigInteger preservedId) {
        for(int i = fingerTable.length - 1; i >= 0; i--  )
            if(fingerTable[i] != null && fallsBetween(fingerTable[i].getNodeId(), this.getNodeId(), preservedId))
                return fingerTable[i];
        return this;
    }

    /////////////////////////////////////////////
    ////////  CHORD MAINTENANCE SECTION  ////////
    /////////////////////////////////////////////

    /**
     * STABILIZE
     * called periodically, verifies n's immediate
     * successor, and tells the successor about n
     */
    public void stabilize(){
        //get predecessor

        stabilizeX = succNode.requestFindPred(this.getNodeId(),this.getAddress(),this.getPort());

        //TO DEBUG
        //System.out.println("STABILIZE - locking...");

        try {
            latchStabilize.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        latchStabilize =  new CountDownLatch(1);

        //TO DEBUG
        if(stabilizeX != null) {
            //System.out.println("STABILIZED - "+stabilizeX.getNodeId());
        }
        else {
            //System.out.println("UNLOCKED");
        }

        //check if X falls between (n,successor)
        if(stabilizeX != null
                && ! this.getNodeId().equals(stabilizeX.getNodeId() )
                && ( fallsBetween(stabilizeX.getNodeId(), this.getNodeId(), succNode.getNodeId()) || this.getNodeId().equals(succNode.getNodeId()) )
        ){
            fingerTable[0] = stabilizeX;
            succNode = stabilizeX;
        }

        succNode.requestNotify(this.getNodeId(), this);
    }

    public static void unlockStabilize(){

        int i=1;
        for( ; i<fingerTable.length; i++){
            if( ! fingerTable[i].getNodeId().equals(succNode.getNodeId())  )
                break;
        }

        BigInteger oldSuccId = fingerTable[0].getNodeId();


        if(i == fingerTable.length) {
            if (Peer.predNode.getNodeId().equals(oldSuccId)) {
                succNode = Peer.getPeer();
            } else {
                succNode = Peer.predNode;
            }
        }
        else succNode = fingerTable[i];

        for(int j = 0; j < fingerTable.length; j++){
            if(fingerTable[j].getNodeId().equals(oldSuccId) ){
                fingerTable[j] = succNode;
            }
        }

        latchStabilize.countDown();
    }

    public static void updateSetStabilizeX(Node n){
        stabilizeX = n;
        latchStabilize.countDown();
    }

    /**
     * NOFITFY
     * n' thinks it might be our predecessor
     */
    public void notify(Node node){
         /* PSEUDO CODE -
            if(predecessor is nil or n' falls into predecessor,n))
                predecessor = n';
         */
         if ( predNode == null
                 || fallsBetween(node.getNodeId(), predNode.getNodeId(), this.getNodeId() )
                 || !predNode.getNodeId().equals(this.getNodeId())){

             if (node.getNodeId().equals(this.getNodeId()))
                 return;

             if (predNode == null || !node.getNodeId().equals(predNode.getNodeId())) {
                 predNode = node;

                 if (succNode.getNodeId().equals(this.getNodeId())) {
                     succNode = node;
                     fingerTable[0] = node;
                 }
             }
         }
    }

    /**
     * FIX FINGERS
     *  called periodically, refreshes finger table entries.
     *  next stores the index of the next finger to fix.
     */
    public void fixFingers(){

        for (int i = 1; i < fingerTable.length; i++) {
            Node n = findSuccFinger(this.getAddress(),this.getPort(),getFinger(i),i);

            if(n!= null)
                updateFinger(n,i);
        }

    }

    public static void updateFinger(Node node, int fingerId){
        fingerTable[fingerId] = node;
    }

    /**
     * CHECK PREDECESSOR
     * called periodically, checks whether predecessor has failed
     */
    public void checkPred(){
        /* PSEUDO CODE
        if(predecessor has failed)
            predecessor = nil
         */

        MessageFactory messageFactory = new MessageFactory();
        Message msg = messageFactory.checkPredMsg();

        Peer.getThreadExecutor().execute(new SendMessagesManager(msg, predNode.getAddress(), predNode.getPort()));
    }

    public BigInteger getFinger(int i){
        // (n + 2^k-1)mod 2^m
        //n - this node, m - finger length
        return  (this.getNodeId().add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(fingerTable.length));
        //return ((this.getNodeId().add(new BigInteger("2").pow(i))).mod(new BigInteger("2").pow(fingerTable.length)));
    }

    /////////////////////////////////////////
    //////// BACKUP PROTOCOL SECTION ////////
    /////////////////////////////////////////

    /**
     *
     * Backup Service methods
     */

    @Override
    public synchronized String backup(String file_path, Integer replication_degree) {
        //File creation
        FileInfo file = new FileInfo(file_path);
        file.prepareChunks(replication_degree);

        //File store
        storage.storeFile(file);

        //Send PUTCHUNK message for each file's chunk
        Iterator<Chunk> chunkIterator = file.getChunks().iterator();
        MessageFactory messageFactory = new MessageFactory();

        while(chunkIterator.hasNext()) {
            Chunk chunk = chunkIterator.next();
            Message msg = messageFactory.putChunkMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), chunk, replication_degree);

            for (int i = 0; i < chunk.getDesired_replication_degree(); i++) {
                Node destNode;
                if (i < fingerTable.length)
                    destNode = fingerTable[i];
                else destNode = fingerTable[0];
                threadExecutor.execute(new SendMessagesManager(msg, destNode.getAddress(), destNode.getPort()));
                msg.printSentMessage();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String chunkKey = chunk.getFile_id() + "-" + chunk.getChunk_no();
                PutChunkAttempts putChunkAttempts = new PutChunkAttempts(1, 5, msg, chunkKey, replication_degree, destNode);
                Peer.getThreadExecutor().schedule(putChunkAttempts, 1, TimeUnit.SECONDS);
            }
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
                while(chunkIterator.hasNext()){
                    Chunk chunk = chunkIterator.next();
                    //Prepare message to send
                    MessageFactory messageFactory = new MessageFactory();
                    Message msg = messageFactory.getChunkMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), fileInfo.getFileId(), chunk.getChunk_no());

                    String chunkKey = fileInfo.getFileId()+'-'+chunk.getChunk_no();
                    if (Peer.getPeer().getStorage().getPeers_with_chunks().containsKey(chunkKey)) {
                        for (int j = 0; j < getStorage().getPeers_with_chunks().get(chunkKey).size(); j++) {
                            String[] destPeer = getStorage().getPeers_with_chunks().get(chunkKey).get(j);

                            //Send message
                            Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destPeer[0], Integer.parseInt(destPeer[1])));
                            msg.printSentMessage();
                        }
                    }
                    else return "Chunk was not backed up previously";
                }

                Peer.getThreadExecutor().execute(new RestoreChunks(file, fileInfo.getChunks().size()));
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
        for (int i = 0; i < storage.getStoredFiles().size(); i++) {
            if (storage.getStoredFiles().get(i).getFile().getName().equals(file_path)) {
                file_exists = true;

                //Get previously backed up file
                fileInfo = storage.getStoredFiles().get(i);

                //Get file chunks
                Set<Chunk> chunks = fileInfo.getChunks();
                Iterator<Chunk> chunkIterator = chunks.iterator();
                MessageFactory messageFactory = new MessageFactory();

                //For each chunk
                while (chunkIterator.hasNext()) {
                    Chunk chunk = chunkIterator.next();

                    //Prepare message to send
                    Message msg = messageFactory.deleteMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), chunk);

                    String chunkKey = fileInfo.getFileId()+'-'+chunk.getChunk_no();
                    if (Peer.getPeer().getStorage().getPeers_with_chunks().containsKey(chunkKey)) {
                        ArrayList<String[]> peers_with_chunk = Peer.getPeer().getStorage().getPeers_with_chunks().get(chunkKey);
                        for (int j = 0; j < peers_with_chunk.size(); j++) {
                            String[] destNode = peers_with_chunk.get(j);
                            //Send message
                            Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destNode[0], Integer.parseInt(destNode[1])));
                            System.out.println("To "+destNode[0]+":"+destNode[1]);
                            msg.printSentMessage();
                        }
                        storage.remove_entry_peer_chunks(chunkKey);
                    }
                }
                //Delete file
                storage.deleteFile(fileInfo);

                break;
            }
        }

        if (file_exists)
            return "Delete successful";
        else return "File does not exist";
    }

    @Override
    public synchronized String reclaim(Integer max_space) {
        System.out.println("Space Requested: "  + max_space);
        double spaceUsed = storage.getOccupiedSpace();
        System.out.println("Space Used: "  + spaceUsed);

        double spaceClaimed = max_space * 1000; //The client shall specify the maximum disk space in KBytes (1KByte = 1000 bytes)
        System.out.println("Space Claimed: "  + spaceClaimed);

        double tmpSpace = spaceUsed - spaceClaimed;

        if (tmpSpace > 0) {
            double deletedSpace = 0;
            Iterator<Chunk> chunkIterator = storage.getStoredChunks().iterator();
            MessageFactory messageFactory = new MessageFactory();

            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                System.out.println("Deleted Space: " + deletedSpace);
                System.out.println("Temp Space: " + tmpSpace);

                if (deletedSpace < tmpSpace) {
                    deletedSpace += chunk.getChunk_size();

                    Message msg = messageFactory.reclaimMsg(Peer.getPeer().getAddress(), Peer.getPeer().getPort(), chunk);

                    // Send reclaim message to chunk owner
                    Node destNode = chunk.getOwner();
                    //Send message
                    Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destNode.getAddress(), destNode.getPort()));
                    msg.printSentMessage();

                    String filepath = storage.getDirectory().getPath() + "/file" + chunk.getFile_id() + "/chunk" + chunk.getChunk_no();
                    File file = new File(filepath);
                    file.delete();
                    storage.deleteDirectory(chunk.getFile_id());

                    String chunkKey = chunk.getFile_id() + "-" + chunk.getChunk_no();
                    storage.decrementChunkOccurences(chunkKey);

                    chunkIterator.remove();
                }
                else {
                    break;
                }
                System.out.println(" ");
            }

            storage.reclaimSpace(max_space);

        }

        return "Reclaim successful";
    }

    @Override
    public synchronized Storage state() {
        //For each file whose backup it has initiated
        System.out.println("-> For each file whose backup it has initiated:");
        for (int i = 0; i < storage.getStoredFiles().size(); i++) {
            FileInfo fileInfo = storage.getStoredFiles().get(i);

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
                int repDegree = storage.getChunkCurrentDegree(chunk_id);
                System.out.println("\t  Perceived replication degree: "+repDegree);

            }
        }

        //For each chunk it stores
        System.out.println("-> For each chunk it stores:");
        for (int i = 0; i < storage.getStoredChunks().size(); i++) {
            Chunk chunk = storage.getStoredChunks().get(i);

            //Chunk id
            String chunk_id = chunk.getFile_id()+"-"+chunk.getChunk_no();
            System.out.println("\tChunk id: "+chunk_id);

            //Size
            int size = chunk.getChunk_size();
            System.out.println("\tChunk size: "+size);

            //Perceived replication degree
            int repDeg = storage.getChunkCurrentDegree(chunk_id);
            System.out.println("\tPerceived replication degree: "+repDeg);
        }

        return storage;
    }

    public void printFingerTable(){

        System.out.println("\nStatus:");

        System.out.println("SUCC - "+succNode.getNodeId());
        if(predNode != null)
            System.out.println("PRED - "+predNode.getNodeId());
        else System.out.println("PRED - null");
        System.out.println(" ");
    }
}
