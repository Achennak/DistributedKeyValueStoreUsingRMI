import java.rmi.Remote;

/**
 * Interface defining the behavior of a coordinator for a distributed system.
 */
public interface Coordinator extends Remote {

    /**
     * Implements the Two-phase commit protocol.
     *
     * @param operation the operation to be executed.
     * @param key the key associated with the operation.
     * @param value the value associated with the operation.
     * @return a status code indicating success (200) or failure (500).
     */
    String twoPhaseProtocol(String operation, String key, String value);

    /**
     * Registers a server instance with the coordinator.
     *
     * @param server the server instance to be registered.
     */
    void registerNewServer(Server server);

}
