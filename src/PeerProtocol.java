import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PeerProtocol {
    //Args
    private static String protocol_version;
    private static Integer peer_id;
    private static String acc_point;
    private static Peer peer;
    private static ScheduledThreadPoolExecutor threadExecutor;

    public static void main(String args[]) {
        System.out.println("Starting Peer Protocol");
        //Parse args
        if (!parseArgs(args))
            return;

        //Parse channels
        Channel[] channels = new Channel[3];
        try {
            Channel MC = new Channel(args[3], args[4]);
            channels[0] = MC;
            Channel MDB = new Channel(args[5], args[6]);
            channels[1] = MDB;
            Channel MDR = new Channel(args[7], args[8]);
            channels[2] = MDR;
        }
        catch (IOException e) {
            System.out.println(e.toString());
            return;
        }

        //Create executor
        threadExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

        //Create initiator peer
        peer = new Peer(peer_id, channels);
        System.out.println("Started peer with id " + peer_id);

        //Establish RMI communication between TestApp and Peer
        establishCommunication(peer);

        //Initiate or load storage for initiator peer
        if (!loadPeerStorage())
            peer.initiateStorage();

        //Safe and exit
        Runtime.getRuntime().addShutdownHook(new Thread(PeerProtocol::savePeerStorage));
    }

    public static boolean parseArgs(String[] args) {
        //Check the number of arguments given
        if (args.length != 9) {
            System.out.println("Usage: java PeerProtocol version peer_id access_point MC MDB MDR");
            System.out.println("Each channel in format address port");
            return false;
        }

        //Parse protocol version
        protocol_version = args[0];
        if (protocol_version.length() != 3 || (!protocol_version.equals("1.0") && !protocol_version.equals("2.0"))) {
            System.out.println("Version in format <n>.<m>");
            System.out.println("Ony versions 1.0 or 2.0 admitted");
            return false;
        }
        //Parse peer id
        peer_id = Integer.parseInt(args[1]);
        //Parse access point
        acc_point = args[2];

        return true;
    }

    public static void establishCommunication(Peer peer) {
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

    public static String getProtocol_version() {
        return protocol_version;
    }

    public static Peer getPeer() {
        return peer;
    }

    public static ScheduledThreadPoolExecutor getThreadExecutor() {
        return threadExecutor;
    }

    private static void savePeerStorage() {
        try {
            Storage storage = PeerProtocol.getPeer().getStorage();
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
            PeerProtocol.getPeer().setStorage((Storage) in.readObject());
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
}
