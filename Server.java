import java.util.UUID;

/**
 * Interface representing a server in the distributed key-value store system.
 */
public interface Server {
    /**
     * Method for the Prepare phase of the Two-Phase Commit (2PC) protocol.
     * @param operation The type of operation.
     * @param key The key used.
     * @param clientId The client ID making the operation.
     * @return True or false based on the success of the Prepare phase.
     */
    boolean prepare(String operation, String key, UUID clientId);

    /**
     * Method for the Commit phase of the Two-Phase Commit (2PC) protocol.
     * @param operation The type of operation.
     * @param key The key used.
     * @param value The value used.
     * @param clientId The client ID making the operation.
     * @return True or false based on the success of the Commit phase.
     */
    boolean commit(String operation, String key, String value, UUID clientId);

    /**
     * Method for executing the GET request on the server.
     * @param key The key on which the operation is executed.
     * @return The value for the key.
     */
    String executeGet(String key);

    /**
     * Method for executing the PUT request on the server.
     * @param key The key on which the operation is executed.
     * @param value The value to be put.
     */
    void executePut(String key, String value);

    /**
     * Method for executing the DELETE request on the server.
     * @param key The key on which the operation is executed.
     */
    void executeDelete(String key);

}
