import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Implementation of a client for interacting with a remote key-value store server.
 */
public class Client {
    private final static Helper helper = new Helper();

    /**
     * Populates the key-value store with initial data by executing automated PUT, GET, and DELETE operations.
     * @param stub The remote interface of the key-value store server.
     */
    private static void prePopulate(KeyValueStore stub) {
        for (int i = 0; i < 5; i++) {
            try {
                String automatedPut = "PUT key" + i + " v" + (i + 1);
                helper.logMessage("sending message :" + automatedPut);
                stub.put(String.valueOf(i), String.valueOf(i + 1));
                String automatedGet = "GET key" + i;
                helper.logMessage("sending message :" + automatedGet);
                stub.get(String.valueOf(i));
                String automatedDelete = "DELETE key" + i;
                helper.logMessage("sending message :" + automatedDelete);
                stub.delete(String.valueOf(i));
            }catch (RemoteException ex) {
                helper.logError("Communication with Server failed with :"+ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {


        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(null);
            KeyValueStore stub = (KeyValueStore) registry.lookup("KeyValueStore1");
            prePopulate(stub);
            Scanner scanner = new Scanner(System.in);

            String line = "";
            // Prompt user to select a server to connect to
            System.out.println("Enter the server you want to connect to (1-5):");

            line = scanner.nextLine();
            int server = Integer.parseInt(line);

            stub = (KeyValueStore) registry.lookup("KeyValueStore" + server);
            System.out.println(" Connected to server " + server);
            while(true){
                helper.logMessage("Enter operation: PUT <key> <value> or GET <key> or DELETE <key> or SHUTDOWN:");
                line = scanner.nextLine().trim(); // Trim to remove leading/trailing whitespace

                if (line.isEmpty()) {
                    System.out.println("Invalid Input. Please enter a valid operation.");
                    continue;
                }
                String[] results = line.split(" ");
                String method = results[0];

                switch (method.toUpperCase()) {
                    case "GET":
                        if (results.length >= 2) {
                            String key = results[1];
                            String response = stub.get(key);
                            helper.logMessage("Server response :" + response);
                        } else {
                            System.out.println("Invalid GET request. Please provide a key.");
                        }
                        break;
                    case "PUT":
                        if (results.length == 3) {
                            String key = results[1];
                            String value = results[2];
                            if (value.contains(" ") || key.contains(" ")) {
                                System.out.println("Invalid PUT request. Value cannot contain spaces.");
                            } else {
                                String response = stub.put(key, value);
                                helper.logMessage("Server response :" + response);
                            }
                        } else {
                            System.out.println("Invalid PUT request. Please provide both key and value.");
                        }
                        break;
                    case "DELETE":
                        if (results.length >= 2) {
                            String key = results[1];
                            String response = stub.delete(key);
                            helper.logMessage("Server response :" + response);
                        } else {
                            System.out.println("Invalid DELETE request. Please provide a key.");
                        }
                        break;
                    case "SHUTDOWN":
                        System.out.println("Client shutting down gracefully.");
                        System.exit(0); // Exit the client
                    default:
                        System.out.println("Invalid Input.");
                        break;
                }
            }
        } catch (Exception ex) {
            helper.logError("Communication with Server failed with :"+ex.getMessage());
        }
    }
}