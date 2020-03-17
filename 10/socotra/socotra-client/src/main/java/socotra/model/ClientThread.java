package socotra.model;

import socotra.Client;
import socotra.common.ConnectionData;
import socotra.util.SendThread;
import socotra.util.SetOnlineUsers;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;

public class ClientThread extends Thread {

    /**
     * The controller of login page.
     */
    private final LoginRegistrationModel loginRegistrationModel;
    /**
     * Current connected server name.
     */
    private final String serverName;
    /**
     * Current user's username.
     */
    private final String username;
    /**
     * Current user's password.
     */
    private final String password;
    /**
     * The output stream of connection.
     */
    private ObjectOutputStream toServer;
    /**
     * The input stream of connection.
     */
    private ObjectInputStream fromServer;

    private SSLSocket server;

    private int connectionDataType;

    /**
     * The constructor is given the server's name. It opens a socket connection to the server and extracts it input and
     * out streams.
     *
     * @param serverName The server want to connect.
     * @param loginRegistrationModel The controller of login page.
     * @param username   The user's username.
     * @param password   The user's password.
     */
    public ClientThread(String serverName, LoginRegistrationModel loginRegistrationModel, String username, String password, int connectionDataType) {
        this.serverName = serverName;
        this.loginRegistrationModel = loginRegistrationModel;
        this.username = username;
        this.password = password;
        this.connectionDataType = connectionDataType;

    }

    /**
     * Getter for username.
     *
     * @return The user's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for toServer.
     *
     * @return The output stream of connection.
     */
    public ObjectOutputStream getToServer() {
        return toServer;
    }

    public void endConnection() throws IOException {
        toServer.close();
        fromServer.close();
        server.close();
    }

    private void initTLS() throws Exception {
        String CLIENT_KEY_STORE = "src/main/resources/socotra_client_ks";
        String CLIENT_KEY_STORE_PASSWORD = "socotra";
        System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
        // See handle shake process under debug.
//            System.setProperty("javax.net.debug", "ssl,handshake");

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(CLIENT_KEY_STORE), CLIENT_KEY_STORE_PASSWORD.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        context.init(kmf.getKeyManagers(), trustManagers, null);
        SocketFactory factory = context.getSocketFactory();
        server = (SSLSocket) factory.createSocket(serverName, 50443);
        server.startHandshake();
    }

    /**
     * The thread's job.
     */
    public void run() {
        try {
            initTLS();
            toServer = new ObjectOutputStream(server.getOutputStream());
            fromServer = new ObjectInputStream(server.getInputStream());
            loginRegistrationModel.setErrorType(0);
            toServer.writeObject(new ConnectionData(connectionDataType, username, password));
            while (true) {
                ConnectionData connectionData = (ConnectionData) fromServer.readObject();
                System.out.println("Received connectionData." + connectionData);
                // If receive the result about user's validation.
                System.out.println("connectionDataType : "  + connectionDataType);
                switch (connectionData.getType()) {
                    case -1:
                        if (!connectionData.getValidated()) {
                            if (connectionDataType == 0){
                                loginRegistrationModel.setErrorType(2);
                            }
                            else if(connectionDataType == -1){
                                loginRegistrationModel.setErrorType(3);
                            }
                            endConnection();
                        }
                        synchronized (loginRegistrationModel) {
                            loginRegistrationModel.notify();
                        }
                        break;
                    case -2:
                        System.out.println(connectionData.getUserSignature() + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                        if (connectionData.getIsOnline()) {
                            Client.getHomeModel().appendClientsList(connectionData.getUserSignature());
                        } else {
                            Client.getHomeModel().removeClientsList(connectionData.getUserSignature());
                        }
                        break;
                    case -3:
                        System.out.println(connectionData.getOnlineUsers());
                        SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
                        Client.setSetOnlineUsers(setOnlineUsers);
                        setOnlineUsers.start();
                        break;
                    case -4:
                        Client.getHomeModel().updateChatData(connectionData.getUuid(), connectionData.getUserSignature());
                        break;
                    case 1:
                    case 2:
                        Client.getHomeModel().appendChatData(connectionData);
                        new SendThread(new ConnectionData(connectionData.getUuid(), connectionData.getToUsername(), connectionData.getUserSignature())).start();
                        break;
                    default:
                        System.out.println("Unknown data.");
                }
            }
        } catch (IOException e) {
            loginRegistrationModel.setErrorType(1);
            e.printStackTrace();
            System.out.println("Socket communication broke.");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected Error.");
        } finally {
            System.out.println("Finally handled.");
            // Notify the loginModel thread anyway.
            synchronized (loginRegistrationModel) {
                loginRegistrationModel.notify();
            }
            try {
                endConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
