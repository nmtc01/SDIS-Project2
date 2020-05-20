import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer extends Node implements PeerInterface{

    //testing chord
    private static int m = 4; //256

    //Args
    private static Peer peer;
    private static Storage storage;
    private static String acc_point;
    private static String ipAddress;
    private static int port;
    private static String initIpAddress;
    private static int initPort;
    private static Node[] fingerTable = new Node[m];
    private static ScheduledThreadPoolExecutor threadExecutor; //TODO use this instead of thread

    public static Node predNode;
    public static Node succNode;

    private static Node stabilizeX;
    public static boolean predActive = false;

    //lock joinning thread
    public static final CountDownLatch latchJoin = new CountDownLatch(1);
    public static  CountDownLatch latchStabilize = new CountDownLatch(1);

    public Peer(String ipAddress, int port) {

        //create a new chord ring
        super(ipAddress, port);
        succNode = this;

        System.out.println("Created with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Init Peer - "+this.getAddress()+":"+this.getPort());

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), 5, 5, TimeUnit.SECONDS);
    }

    public Peer(String ipAddress, int port, String initAddress, int initPort) {

        super(ipAddress, port);
        succNode = this;

        System.out.println("Created with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Peer - "+this.getAddress()+":"+this.getPort());

        this.join(new Node(initAddress, initPort));

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), 5, 5, TimeUnit.SECONDS);
    }

    //testing funtions
    public Peer(int order, String ipAddress, int port) {

        //create a new chord ring
        super(order, ipAddress, port);
        succNode = this;

        System.out.println("Created with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Init Peer - "+this.getAddress()+":"+this.getPort());

        this.printFingerTable();

        threadExecutor.scheduleAtFixedRate( new ChordManager(this), 5, 5, TimeUnit.SECONDS);
    }

    public Peer(int order, String ipAddress, int port, String initAddress, int initPort) {

        super(order, ipAddress, port);
        succNode = this;

        System.out.println("Created with Id: "+this.getNodeId());

        Arrays.fill(fingerTable, this);

        System.out.println("Peer - "+this.getAddress()+":"+this.getPort());

        this.join(new Node(initAddress, initPort));

        this.printFingerTable();

         threadExecutor.scheduleAtFixedRate( new ChordManager(this), 5, 5, TimeUnit.SECONDS);
    }

    public static void setSuccNode(Node sn){
        succNode = sn;
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
/*
        if (args.length == 5) {
            peer = new Peer(ipAddress, port, initIpAddress, initPort);
        }
        else {
            peer = new Peer(ipAddress, port);
        }
*/

       if (args.length == 6) {  // change to 6
            peer = new Peer(Integer.parseInt(args[5]),ipAddress, port, initIpAddress, initPort);
        }
        else {
            peer = new Peer(Integer.parseInt(args[3]),ipAddress, port);
        }

        System.out.println("Started peer");

        //Establish RMI communication between TestApp and Peer
        //establishRMICommunication(peer);

        //Initiate or load storage for initiator peer
        if (!loadPeerStorage()) {
            initiateStorage();
        }

        //Safe and exit
        Runtime.getRuntime().addShutdownHook(new Thread(Peer::savePeerStorage));
    }

    public static boolean parseArgs(String[] args) {
        //Check the number of arguments given
        if (args.length != 4 && args.length != 6) { //change to 3 - 5
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
        if (args.length == 6) { //change to 5
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
            String filenameUnix = "Storage/storage.ser";

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

        for (int i = 0; i < fingerTable.length; i++) {
            if (fallsBetween(getFinger(i), this.getNodeId(), succNode.getNodeId()))
                fingerTable[i] = succNode;
            else
                fingerTable[i] = this;
        }

    }

    //todo check if this is right
    public boolean fallsBetween(BigInteger id, BigInteger n, BigInteger succ){

        if (n.compareTo(succ) > 0)
            return (id.compareTo(n) > 0) || (id.compareTo(succ) <= 0);    // 5.compareTo(4) => 1

        return (id.compareTo(n) > 0) && (id.compareTo(succ) <= 0);

        /*
        //if n major than succ cannot be compared
        if(n.compareTo(succ) < 0)
            return false;
        else if(id.compareTo(n) >= 0 && id.compareTo(succ) <= 0)
            return true;
        else
            return false;
        */

    }

    public Node findSucc(String address, int port, BigInteger id){

        //case its the same id return itself

        if(succNode.getNodeId().equals(this.getNodeId())){
            if(!id.equals(this.getNodeId())){
                //succNode = new Node(address,port); //TODO use this
                succNode = new Node(id,address,port);
                //predNode = new Node(address,port); //TODO use this
                predNode = new Node(id,address,port);
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

    public void setPred(Node node){predNode=node;}

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

        System.out.println("STABILIZE - locking...");

        try {
            latchStabilize.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        latchStabilize =  new CountDownLatch(1);

        if(stabilizeX != null)
            System.out.println("STABILIZED - "+stabilizeX.getNodeId());
        else System.out.println("UNLOCKED");

        //check if X falls between (n,successor)

        if(stabilizeX != null
                && !this.getNodeId().equals(stabilizeX.getNodeId())
                && ( fallsBetween(stabilizeX.getNodeId(), this.getNodeId(), succNode.getNodeId())
                    || this.getNodeId().equals(succNode.getNodeId() ) )
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

        if(i == fingerTable.length)
            succNode = Peer.getPeer();
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
                 || fallsBetween(node.getNodeId(), predNode.getNodeId(), this.getNodeId())
                 || !predNode.getNodeId().equals(this.getNodeId())
         ){
             if (node.getNodeId().equals(this.getNodeId()))
                 return;

             if (predNode == null || !node.getNodeId().equals(predNode.getNodeId())) {
                 predNode = node;

                 //todo function to update storage, give storage

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
        /*
        predActive = false;
        predNode.testResponse(this.getNodeId(),this.getAddress(),this.getPort());
        try {
            Thread.sleep(1);

            if(!predActive){
                predNode=null;
                System.out.println("Pred not found");
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
        //predNode = null;

        DataOutputStream dos;
        byte[] msg = ("bleh").getBytes();

        System.out.println("TEST PRED");
        //Create socket
        //TODO check this
        try {
            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(predNode.getAddress(), predNode.getPort());

            dos = new DataOutputStream(sslSocket.getOutputStream());

            dos.flush();
            dos.write(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //String reply = in.readLine();

            sslSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            predNode = null;
            System.out.println("Failed to connect to predecessor");
        }


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
            byte msg[] = messageFactory.putChunkMsg(chunk, replication_degree);

            for (int i = 0; i < chunk.getDesired_replication_degree(); i++) {
                //TODO check this
                Node destNode = fingerTable[i];
                threadExecutor.execute(new SendMessagesManager(msg, destNode.getAddress(), destNode.getPort()));

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String chunkKey = chunk.getFile_id() + "-" + chunk.getChunk_no();
                PutChunkAttempts putChunkAttempts = new PutChunkAttempts(1, 5, msg, chunkKey, replication_degree);
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
                    byte[] msg = messageFactory.getChunkMsg(fileInfo.getFileId(), chunk.getChunk_no());

                    // TODO Select destination peer, use random index instead of 0? - Check
                    String chunkKey = fileInfo.getFileId()+'-'+chunk.getChunk_no();
                    if (getStorage().getPeers_with_chunks().containsKey(chunkKey)) {
                        Peer destPeer = getStorage().getPeers_with_chunks().get(chunkKey).get(0);

                        //Send message
                        Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destPeer.getAddress(), destPeer.getPort()));
                    }
                    else return "Chunk was not backed up previously";
                }

                //TODO check - blocks here
                while(fileInfo.getChunks().size() != storage.getRestoreChunks().size()) {}

                Peer.getThreadExecutor().execute(new RestoreChunks(file));
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
                storage.getDeletedFiles().add(fileInfo);

                //Get file chunks
                Set<Chunk> chunks = fileInfo.getChunks();
                Iterator<Chunk> chunkIterator = chunks.iterator();
                MessageFactory messageFactory = new MessageFactory();

                //For each chunk
                while (chunkIterator.hasNext()) {
                    Chunk chunk = chunkIterator.next();

                    //Prepare message to send
                    byte msg[] = messageFactory.deleteMsg(chunk);

                    for (int j = 0; j < chunk.getDesired_replication_degree(); j++) {
                        Node destNode = fingerTable[j];

                        //Send message
                        Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destNode.getAddress(), destNode.getPort()));
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

        double spaceUsed = storage.getOccupiedSpace();
        double spaceClaimed = max_space; //The client shall specify the maximum disk space in KBytes (1KByte = 1000 bytes)

        double tmpSpace = spaceUsed - spaceClaimed;

        if (tmpSpace > 0) {
            double deletedSpace = 0;
            Iterator<Chunk> chunkIterator = storage.getStoredChunks().iterator();
            MessageFactory messageFactory = new MessageFactory();

            while (chunkIterator.hasNext()) {
                Chunk chunk = chunkIterator.next();
                if (deletedSpace < tmpSpace || max_space == 0) {
                    deletedSpace += chunk.getChunk_size();

                    byte msg[] = messageFactory.reclaimMsg(chunk);

                    //TODO send reclaim message to every peer on the system?
                    for (int j = 0; j < fingerTable.length; j++) {
                        Node destNode = fingerTable[j];

                        //Send message
                        Peer.getThreadExecutor().execute(new SendMessagesManager(msg, destNode.getAddress(), destNode.getPort()));
                    }

                    String filepath = storage.getDirectory().getPath() + "/file" + chunk.getFile_id() + "/chunk" + chunk.getChunk_no();
                    File file = new File(filepath);
                    file.delete();
                    String chunkKey = chunk.getFile_id() + "-" + chunk.getChunk_no();
                    storage.decrementChunkOccurences(chunkKey);

                    // TODO: Select destination peer
                    if (this.storage.getChunkCurrentDegree(chunkKey) < chunk.getDesired_replication_degree()) {
                        byte msg2[] = messageFactory.putChunkMsg(chunk, chunk.getDesired_replication_degree());
                        Random random = new Random();
                        int random_value = random.nextInt(401);

                        for (int i = this.storage.getChunkCurrentDegree(chunkKey); i < chunk.getDesired_replication_degree(); i++) {
                            //TODO check this - cannot send this to me(peer) again
                            Node destNode = fingerTable[i];
                            threadExecutor.schedule(new SendMessagesManager(msg2, destNode.getAddress(), destNode.getPort()),random_value, TimeUnit.MILLISECONDS);
                        }
                    }
                    chunkIterator.remove();
                }
                else {
                    break;
                }
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

        System.out.println("\nPrinting Finger Table...");
        for(int i=0; i < fingerTable.length; i++){
            System.out.println(" - F["+i+"] - " +fingerTable[i].getNodeId());
        }

        System.out.println("SUCC - "+succNode.getNodeId());
        if(predNode != null)
            System.out.println("PRED - "+predNode.getNodeId());
        else System.out.println("PRED - null");
        System.out.println(" ");
    }

    public void printFingerTable(){

        System.out.println("\nPrinting Finger Table...");
        for(int i=0; i < fingerTable.length; i++){
            System.out.println(" - F["+i+"] - " +fingerTable[i].getNodeId());
        }

        System.out.println("SUCC - "+succNode.getNodeId());
        if(predNode != null)
            System.out.println("PRED - "+predNode.getNodeId());
        else System.out.println("PRED - null");
        System.out.println(" ");
    }
}
