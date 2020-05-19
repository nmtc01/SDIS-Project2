import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;

public class TestApp {
    private static String remote_object_name;
    private static String sub_protocol;
    private static String operand1;
    private static Integer operand2;

    public static boolean parseArgs(String[] args) {
        //Check the number of arguments given
        if (args.length < 2 || args.length > 4) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
            return false;
        }
        //Parse sub_protocol
        sub_protocol = args[1];
        //Check right arguments for sub_protocol
        if ((args.length != 4 && sub_protocol.equals("BACKUP")) || (args.length != 2 && sub_protocol.equals("STATE"))) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
            return false;
        }

        //Parse peer_ap
        remote_object_name = args[0];
        //Parse operands
        if (!sub_protocol.equals("STATE"))
            operand1 = args[2];
        if (sub_protocol.equals("BACKUP"))
            operand2 = Integer.parseInt(args[3]);

        return true;
    }

    public static void main(String[] args) {
        //Parse args
        if (!parseArgs(args))
            return;

        try {
            Registry registry = LocateRegistry.getRegistry(); //default port 1099
            PeerInterface peer = (PeerInterface) registry.lookup(remote_object_name);

            //Choose the path
            String response = new String();
            Storage state_response;
            switch (sub_protocol) {
                case "BACKUP": {
                    response = peer.backup(operand1, operand2);
                    break;
                }
                case "RESTORE": {
                    response = peer.restore(operand1);
                    break;
                }
                case "DELETE": {
                    response = peer.delete(operand1);
                    break;
                }
                case "RECLAIM": {
                    response = peer.reclaim(Integer.parseInt(operand1));
                    break;
                }
                case "STATE": {
                    state_response = peer.state();

                    //For each file whose backup it has initiated
                    System.out.println("-> For each file whose backup it has initiated:");
                    for (int i = 0; i < state_response.getStoredFiles().size(); i++) {
                        FileInfo fileInfo = state_response.getStoredFiles().get(i);

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
                            int repDegree = state_response.getChunkCurrentDegree(chunk_id);
                            System.out.println("\t  Perceived replication degree: "+repDegree);

                        }
                    }

                    //For each chunk it stores
                    System.out.println("-> For each chunk it stores:");
                    for (int i = 0; i < state_response.getStoredChunks().size(); i++) {
                        Chunk chunk = state_response.getStoredChunks().get(i);

                        //Chunk id
                        String chunk_id = chunk.getFile_id()+"-"+chunk.getChunk_no();
                        System.out.println("\tChunk id: "+chunk_id);

                        //Size
                        int size = chunk.getChunk_size();
                        System.out.println("\tChunk size: "+size);

                        //Perceived replication degree
                        int repDeg = state_response.getChunkCurrentDegree(chunk_id);
                        System.out.println("\tPerceived replication degree: "+repDeg);
                    }

                    break;
                }
            }

            System.out.println(response);
        } catch (Exception e) {
            System.err.println("TestApp exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
