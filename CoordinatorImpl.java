import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implementation of the Coordinator interface responsible for coordinating transactions between servers.
 */
public class CoordinatorImpl implements Coordinator {
    List<Server> servers;
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final Helper helper;

    public CoordinatorImpl() throws RemoteException {
        super();
        this.helper = new Helper();
        this.servers = new ArrayList<>();
    }

    @Override
    public void registerNewServer(Server server) {
        servers.add(server);
    }

    @Override
    public String twoPhaseProtocol(String operation, String key, String value) {
        UUID clientId = UUID.randomUUID();
        if(!executePreparePhase(operation, key, clientId)){
            System.out.println("Server is busy. Please try again later.");
            System.out.println("Aborting operation : "+operation);
            return "500";
        }
        else if(!executeCommitPhase(operation, key, value, clientId)){
            System.out.println("Error occurred during commit");
            System.out.println("Aborting commit operation.");
            return "500";
        }

        if(operation.equals("GET")){
            return "200";
        }
        else if(operation.equals("PUT")){
            // Execute PUT request on all server replicas
            for(Server server: servers) {
                executorService.submit(() -> {
                    server.executePut(key, value);
                });
            }
        }
        else{
            // Execute DELETE request on all server replicas
            for(Server server: servers) {
                executorService.submit(() -> {
                    server.executeDelete(key);
                });
            }
        }
        return "200";
    }

    /**
     * Executes the commit phase of the two-phase commit protocol.
     * @param operation the operation to be executed.
     * @param key the key associated with the operation.
     * @param value the value associated with the operation.
     * @param clientId the unique identifier of the client.
     * @return true if the commit phase is successful for all servers, false otherwise.
     */
    private boolean executeCommitPhase(String operation, String key, String value, UUID clientId) {
        List<Future<Boolean>> futures = new ArrayList<>();
       helper.logMessage(" Initiating commit phase for all servers...");

        // Initiate commit phase on all replicas
        for(Server server: servers) {
            Future<Boolean> future = executorService.submit(() -> {
                return server.commit(operation, key, value, clientId);
            });
            futures.add(future);
        }

        int count = 0;
        for(Future<Boolean> future: futures){
            try{
                if(future.get().equals(true)){
                    count += 1;
                }
                else {
                    Thread.sleep(1000);
                    if(future.get().equals(true)){
                        count += 1;
                    }
                }
            } catch (Exception ex) {
               helper.logError("executeCommitPhase failed with :"+ex.getMessage());
            }
        }

        // If all servers are ready, return true
        return count == servers.size();
    }

    /**
     * Executes the prepare phase of the two-phase commit protocol.
     * @param operation the operation to be executed.
     * @param key the key associated with the operation.
     * @param clientId the unique identifier of the client.
     * @return true if the prepare phase is successful for all servers, false otherwise.
     */
    private boolean executePreparePhase(String operation, String key, UUID clientId){
        List<Future<Boolean>> futures = new ArrayList<>();
       helper.logMessage( " Initiating prepare phase for all servers...");

        // Initiate prepare phase on all server replicas
        for(Server server: servers) {
            Future<Boolean> future = executorService.submit(() -> {
                return server.prepare(operation, key, clientId);
            });
            futures.add(future);
        }

        int count = 0;
        for(Future<Boolean> future: futures){
            try{
                if(future.get().equals(true)){
                    count += 1;
                }
                else {
                    Thread.sleep(1000);
                    if(future.get().equals(true)){
                        count += 1;
                    }
                }
            } catch (Exception ex) {
               helper.logError("executePreparePhase failed with :"+ex.getMessage());
            }
        }

        // return true if all servers are prepared
        return count == servers.size();
    }


}
