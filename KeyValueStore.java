import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for the KeyValueStore, representing a distributed key-value store system.
 * Allows clients to perform operations such as get, put, and delete on key-value pairs.
 */
public interface KeyValueStore extends Remote {
    /**
     * Method to get a value from key-value store
     * @param key integer key
     * @return associated string value of key
     * @throws RemoteException if procedure can't be called
     */
    String get(String key) throws RemoteException;

    /**
     * Method to put key-value pair in the key-value store
     * @param key integer key
     * @param value string value
     * @return string success/failure message
     * @throws RemoteException if procedure can't be called
     */
    String put(String key, String value) throws RemoteException;

    /**
     * Method to delete a key from the key-value store
     * @param key integer key
     * @return string success/failure message
     * @throws RemoteException if procedure can't be called
     */
    String delete(String key) throws RemoteException;

}
