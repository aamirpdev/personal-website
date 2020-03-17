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
    private String password;
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
                switch (connectionData.getType()) {
                    case -1:
                        username = connectionData.getUsername();
                        password = connectionData.getPassword();
                        try {
                            if(JdbcUtil.insertUser(username, password)){
                                Server.addClient(username, toClient);
                                System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
                                toClient.writeObject(new ConnectionData(true));
                                TreeSet<String> allClientsName = new TreeSet<>(Server.getClients().keySet());
                                allClientsName.remove(username);
                                // Inform the new client current online users and inform other clients that the new client is online.
                                Server.broadcast(new ConnectionData(username, true), username, new ConnectionData(allClientsName));
                            } else {
                                System.out.println("User already registered");
                                toClient.writeObject(new ConnectionData(false));
                                endClient();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0:
                        username = connectionData.getUsername();
                        password = connectionData.getPassword();
                        try {
                            if (!JdbcUtil.validateUser(username, password)) {
                                System.out.println("Invalidated user. Register the user before");
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
                        break;
                    case -2:
                        Server.broadcast(new ConnectionData(username, false), username);
                        Server.removeClient(username);
                        System.out.println("User log out. Current online users: " + Server.getClients().keySet());
                        endClient();
                        return;
                    case -4:
                        Server.privateSend(connectionData, connectionData.getToUsername());
                        break;
                    case 1:
                    case 2:
                        connectionData.setIsSent(true);
                        if (connectionData.getToUsername().equals("all")) {
                            Server.privateSend(new ConnectionData(connectionData.getUuid(), connectionData.getToUsername(), connectionData.getUserSignature()), connectionData.getUserSignature());
                            Server.broadcast(connectionData, connectionData.getUserSignature());
                        } else {
                            if (!Server.getClients().keySet().contains(connectionData.getToUsername())) {
                                Server.privateSend(new ConnectionData(connectionData.getUuid(), connectionData.getToUsername(), connectionData.getUserSignature()), connectionData.getUserSignature());
                            }
                            Server.privateSend(connectionData, connectionData.getToUsername());
                        }
                        break;
                    default:
                        System.out.println("Unknown data.");
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
