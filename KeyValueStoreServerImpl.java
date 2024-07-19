import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the KeyValueStoreServer interface as well as the Server interface.
 * Represents a server in the distributed key-value store system.
 */
public class KeyValueStoreServerImpl extends UnicastRemoteObject implements Server,KeyValueStore {
    private final Map<String, String> keyValueStore;
    private final Coordinator coordinator;
    private UUID clientId;

    private final Helper helper;
    private final int serverId;
    public KeyValueStoreServerImpl(Coordinator coordinator, int serverId, Helper helper) throws RemoteException{

        this.coordinator = coordinator;
        this.serverId = serverId;
        this.helper = helper;
        this.coordinator.registerNewServer(this);
        keyValueStore = new ConcurrentHashMap<>();
        clientId = null;
    }


    public String executeGet(String key){
        return keyValueStore.get(key);
    }

    public void executePut(String key, String value){
        keyValueStore.put(key, value);
    }

    public void executeDelete(String key){
        keyValueStore.remove(key);
    }

    @Override
    public String get(String key) throws RemoteException {
      helper.logMessage(" Server " + serverId + ": Received GET request from client");

        if(!keyValueStore.containsKey(key)) {
            return "Key " + key + " not found. GET request aborted.";
        }

        if(coordinator.twoPhaseProtocol("GET", key, null).equals("400")){
            return "Server is busy. Please try again later.";
        }
        return this.executeGet(key);
    }

    @Override
    public String put(String key, String value) throws RemoteException {

        helper.logMessage(" Server " + serverId + ": Received PUT request from client");

        if(coordinator.twoPhaseProtocol("PUT", key, value).equals("400")){
            return "Server is busy. Please try again later.";
        }

        return "Key " + key + " was successfully inserted";
    }

    @Override
    public String delete(String key) throws RemoteException {
        helper.logMessage( " Server " + serverId + ": Received DELETE request from client");

        if(!keyValueStore.containsKey(key)) {
            return "Key " + key + " not found. DELETE request aborted.";
        }

        if(coordinator.twoPhaseProtocol("DELETE", key, null).equals("400")){
            return "Server is busy. Please try again later.";
        }

        return "Key " + key + " successfully deleted";
    }

    @Override
    public boolean prepare(String operation, String key, UUID cid) {
        helper.logMessage( " Server " + serverId + ": Prepare phase initiated for " + operation
                + " request on " + "key " + key);
        if(this.clientId == null){
            this.clientId = cid;
            return true;
        }
        return false;
    }

    @Override
    public boolean commit(String operation, String key, String value, UUID cid) {
        helper.logMessage(" Server " + serverId + ": Commit phase initiated for " + operation
                + " request on " + key + " key");
        if(this.clientId == cid){
            this.clientId = null;
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws RemoteException {

        if (args.length < 5) {
            System.err.println("Insufficient number of ports provided. Please provide 5 ports.");
            System.exit(1);
        }


        List<Integer> listOfPorts = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            listOfPorts.add(Integer.parseInt(args[i]));
        }
        // Create registry on default port
        Registry registry = LocateRegistry.createRegistry(1099);

        Coordinator coordinator = new CoordinatorImpl();

        // Instantiate 5 servers on different ports
        for(int i = 0; i < 5; i++) {
            KeyValueStoreServerImpl  keyValueStoreServer = new KeyValueStoreServerImpl(coordinator, i + 1, new Helper());

            try {
                // Bind the remote object to the registry for RPC communication
                registry.bind("KeyValueStore" + (i + 1), keyValueStoreServer);
                System.out.println("Server " + (i+1) + " ready...");
            } catch (AlreadyBoundException e) {
                System.err.println("Remote object could not be bound");
            }

        }

    }
}
