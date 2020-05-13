import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
    String backup(String file, Integer replication_degree) throws RemoteException;
    String restore(String file) throws RemoteException;
    String delete(String file) throws RemoteException;
    String reclaim(Integer max_space) throws RemoteException;
    Storage state() throws RemoteException;
}
