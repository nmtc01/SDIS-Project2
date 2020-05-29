import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements java.io.Serializable {
    private double free_space;
    private double space_used;

    private File directory;
    private ArrayList<FileInfo> storedFiles = new ArrayList<>();
    private ArrayList<Chunk> storedChunks = new ArrayList<>();
    private ConcurrentHashMap<String, byte[]> restoreChunks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> chunks_current_degrees = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ArrayList<String[]>> peers_with_chunks = new ConcurrentHashMap<>();
    private double total_space;

    public Storage() {
        this.free_space = 1000000000;
        this.space_used = 0;
        createPeerDirectory();
    }

    public ConcurrentHashMap<String, ArrayList<String[]>> getPeers_with_chunks() {
        return this.peers_with_chunks;
    }

    private void createPeerDirectory() {
        String root = System.getProperty("user.dir");
        String filepathUnix = "/PeerProtocol/Peer"+Peer.getPeer().getNodeId();
        String pathUnix = root + filepathUnix;

        File tmpUnix = new File(pathUnix);
        this.directory = tmpUnix;

        if (!tmpUnix.exists()) {
            if (tmpUnix.mkdirs()) {
                System.out.println("Created folder for Peer");
            }
        }
    }

    public ArrayList<Chunk> getStoredChunks() {
        return storedChunks;
    }

    public void storeFile(FileInfo file) {
        if (file.getFile().length() > free_space)
            return;

        String fileFolder = directory.getPath() + "/file" + file.getFileId();

        File tmp = new File(fileFolder);
        if (!tmp.exists()) {
            if (tmp.mkdirs()) {
                System.out.println("Created folder for file " + file.getFile().getName() + " inside Peer");
                exportFile(tmp, file.getFile());
                System.out.println("Stored file " + file.getFile().getName() + " inside Peer");
            }
        } else exportFile(tmp, file.getFile());

        //Store on list
        this.storedFiles.add(file);

        //Decrement free space
        //decFreeSpace(file.getFile().length());
    }

    public void restoreFile(File fileIn) {
        if (fileIn.length() > free_space)
            return;

        File fileFolder = new File(directory.getPath() + "/Restored");
        boolean can_export = true;

        if (!fileFolder.exists()) {
            if (fileFolder.mkdirs()) {
                System.out.println("Created folder to restored files");
            }
            else can_export = false;
        }

        if (!can_export)
            return;

        List<String> sortedChunkKeys = new ArrayList<>(this.getRestoreChunks().keySet());
        sortedChunkKeys.sort(new ChunkKeyComparator());
        File fileOut = new File(fileFolder+"/"+fileIn.getName());
        try {
            FileOutputStream myWriter;
            myWriter = new FileOutputStream(fileOut);
            for (String key : sortedChunkKeys) {
                //WRITE
                myWriter.write(this.restoreChunks.get(key));
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.restoreChunks.clear();

        //decFreeSpace(fileOut.length());
    }

    public void exportFile(File directory, File fileIn) {
        try {
            File fileOut = new File(directory.getPath() + "/" + fileIn.getName());

            //READ
            FileInputStream myReader = new FileInputStream(fileIn);
            byte[] input = new byte[(int) fileIn.length()];
            myReader.read(input);
            myReader.close();

            //WRITE
            FileOutputStream myWriter = new FileOutputStream(fileOut);
            myWriter.write(input);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteChunk(String fileId) {
        for (Iterator<Chunk> chunkIterator = this.storedChunks.iterator(); chunkIterator.hasNext(); ) {
            Chunk chunk = chunkIterator.next();

            if (chunk.getFile_id().equals(fileId)) {
                String file_path = directory.getPath() + "/file" + chunk.getFile_id() + "/chunk" + chunk.getChunk_no();
                File file = new File(file_path);
                file.delete();

                free_space += chunk.getChunk_size();

                chunkIterator.remove();
                decrementChunkOccurences(chunk.getFile_id()+"-"+chunk.getChunk_no());

                remove_peer_chunks(chunk.getFile_id()+"-"+chunk.getChunk_no(), Peer.getPeer().getAddress(), Peer.getPeer().getPort());
            }
        }
        deleteDirectory(fileId);
    }

    public void deleteDirectory(String fileId) {
        String fileFolder = directory.getPath() + "/file" + fileId;
        File folder = new File(fileFolder);
        folder.delete();
    }

    public void storeChunk(Chunk chunk) {
        if (chunk.getContent().length > free_space)
            return;

        //Store on system
        String fileFolder = directory.getPath() + "/file" + chunk.getFile_id();

        File tmp = new File(fileFolder);
        if (!tmp.exists()) {
            if (tmp.mkdirs()) {
                exportChunk(tmp, chunk);
            }
        } else exportChunk(tmp, chunk);

        //Store on hashmap and list
        String key = chunk.getFile_id() + "-" + chunk.getChunk_no();
        incrementChunkOccurences(key);
        this.storedChunks.add(chunk);

        //Decrement free space
        decFreeSpace(chunk.getContent().length);
        //Add to peers_with_chunks
        add_peer_chunks(chunk.getFile_id()+"-"+chunk.getChunk_no(), Peer.getPeer().getAddress(), Peer.getPeer().getPort());
    }

    public void exportChunk(File directory, Chunk chunk) {
        try {
            File fileOut = new File(directory.getPath() + "/" + "chunk" + chunk.getChunk_no());

            byte[] input = chunk.getContent();

            //WRITE
            FileOutputStream myWriter = new FileOutputStream(fileOut);
            myWriter.write(input);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(String fileId, int chunkNo) {
        ArrayList<Chunk> storedChunks = this.getStoredChunks();

        for (int i = 0; i < storedChunks.size(); i++) {
            Chunk chunk = storedChunks.get(i);
            if (chunk.getFile_id().equals(fileId) && chunk.getChunk_no() == chunkNo) {
                return true;
            }
        }

        return false;
    }

    public void incrementChunkOccurences(String key) {
        if (this.chunks_current_degrees.containsKey(key)) {
            this.chunks_current_degrees.replace(key, this.chunks_current_degrees.get(key) + 1);
        } else {
            this.chunks_current_degrees.put(key, 1);
        }
    }

    public void decrementChunkOccurences(String key) {
        if (this.chunks_current_degrees.containsKey(key) && this.chunks_current_degrees.get(key) != 0) {
            this.chunks_current_degrees.replace(key, this.chunks_current_degrees.get(key) - 1);
        }
    }

    public int getChunkCurrentDegree(String chunkKey) {
        if (this.chunks_current_degrees.containsKey(chunkKey))
            return this.chunks_current_degrees.get(chunkKey);
        else return 0;
    }

    public synchronized void decFreeSpace(double size) {
        this.free_space -= size;
    }

    public synchronized double getFreeSpace() {
        return free_space;
    }

    public double getOccupiedSpace() {

        for (Chunk chunk : this.storedChunks) {
            space_used += chunk.getChunk_size();
        }
        return space_used;
    }

    public double getTotalSpace() {
        for (Iterator<Chunk> chunkIterator = this.storedChunks.iterator(); chunkIterator.hasNext(); ) {
            Chunk chunk = chunkIterator.next();
            total_space += chunk.getChunk_size();
        }
        return total_space;
    }

    public void reclaimSpace(double max_space) {
        this.free_space = max_space - this.getOccupiedSpace();
    }

    public boolean hasSpace(int chunkSize) {
        if (this.getFreeSpace() >= chunkSize) {
            return true;
        }

        return false;
    }

    public void deleteFile(FileInfo fileInfo) {
        String file_directory = this.directory.getPath() + "/file" + fileInfo.getFileId();
        String file_path = file_directory + "/" + fileInfo.getFile().getName();
        File fileFolder = new File(file_directory);
        File file = new File(file_path);
        file.delete();
        fileFolder.delete();
        for (int i = 0; i < this.storedFiles.size(); i++) {
            if (this.storedFiles.get(i).getFileId().equals(fileInfo.getFileId())) {
                this.storedFiles.remove(i);
                break;
            }
        }
    }

    public ArrayList<FileInfo> getStoredFiles() {
        return this.storedFiles;
    }

    public ConcurrentHashMap<String, byte[]> getRestoreChunks() {
        return restoreChunks;
    }

    public File getDirectory() {
        return this.directory;
    }

    public void add_peer_chunks(String chunkKey, String senderAddress, int senderPort) {
        String[] peer = new String[2];
        peer[0] = senderAddress;
        peer[1] = Integer.toString(senderPort);

        if (this.peers_with_chunks.containsKey(chunkKey)) {
            this.peers_with_chunks.get(chunkKey).add(peer);
        }
        else {
            ArrayList<String[]> peers = new ArrayList<>();
            peers.add(peer);
            this.peers_with_chunks.put(chunkKey, peers);
        }
    }

    public void remove_peer_chunks(String chunkKey, String senderAddress, int senderPort) {
        if (this.peers_with_chunks.containsKey(chunkKey)) {
            for (int i = 0; i < this.peers_with_chunks.get(chunkKey).size(); i++) {
                String[] peer = this.peers_with_chunks.get(chunkKey).get(i);
                if (peer[0].equals(senderAddress) && peer[1].equals(Integer.toString(senderPort))) {
                    this.peers_with_chunks.get(chunkKey).remove(i);
                    return;
                }
            }
        }
    }

    public void remove_entry_peer_chunks(String chunkKey) {
        if (this.peers_with_chunks.containsKey(chunkKey))
            this.peers_with_chunks.remove(chunkKey);
    }

    public class ChunkKeyComparator implements Comparator<String> {

        @Override
        public int compare(String chunkKey1, String chunkKey2) {
            String[] chunk1 = chunkKey1.split("-");
            String[] chunk2 = chunkKey2.split("-");

            int chunkNo1 = Integer.parseInt(chunk1[1]);
            int chunkNo2 = Integer.parseInt(chunk2[1]);

            if (chunkNo1 < chunkNo2)
                return -1;
            if (chunkNo1 > chunkNo2)
                return 1;
            return 0;
        }
    }
}