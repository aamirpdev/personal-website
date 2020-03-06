package socotra.service;

import socotra.Server;
import socotra.common.ConnectionData;
import socotra.jdbc.JdbcUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.TreeSet;

public class ServerThread extends Thread {
    private Socket client;
    private ObjectOutputStream toClient;
    private String username;
    private ObjectInputStream fromClient;


    public ServerThread(Socket client) throws Exception {
        super("ServerThread");
        this.client = client;
        this.toClient = new ObjectOutputStream(client.getOutputStream());
        this.fromClient = new ObjectInputStream(client.getInputStream());
    }

    /**
     * Do something when client needs to disconnect.
     *
     * @throws IOException The IOException when closing the stream.
     */
    private void endClient() throws IOException {
        toClient.close();
        fromClient.close();
        client.close();
    }


    public void run() {
        try {

            while (true) {
                ConnectionData connectionData = (ConnectionData) fromClient.readObject();
                System.out.println("Received data.");
                if (connectionData.getType() == 0) {
                    username = connectionData.getUsername();
                    String password = connectionData.getPassword();
                    try {
                        if (!JdbcUtil.validateUser(username, password)) {
                            System.out.println("Invalidated user.");
                            toClient.writeObject(new ConnectionData(false));
                            endClient();
                            return;
                        } else {
                            Server.addClient(username, toClient);
                            System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
                            toClient.writeObject(new ConnectionData(true));
                            TreeSet<String> allClientsName = new TreeSet<>(Server.getClients().keySet());
                            allClientsName.remove(username);
                            // Inform the new client current online users and inform other clients that the new client is online.
                            Server.broadcast(new ConnectionData(username, true), username, new ConnectionData(allClientsName));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (connectionData.getType() == -2) {
                    Server.broadcast(new ConnectionData(username, false), username);
                    Server.removeClient(username);
                    System.out.println("User log out. Current online users: " + Server.getClients().keySet());
                    endClient();
                    return;
                } else {
                    Server.broadcast(connectionData);
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong. Ending service to client...");
            Server.removeClient(username, toClient);
            System.out.println("User removed. Current online users: " + Server.getClients().keySet());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                endClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
