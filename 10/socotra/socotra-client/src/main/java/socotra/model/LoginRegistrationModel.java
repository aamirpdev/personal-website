package socotra.model;

import socotra.Client;
import socotra.util.Util;

public class LoginRegistrationModel {

    /**
     * The error type of the connection.
     */
    private int errorType = 1;

    /**
     * Setter for error type.
     *
     * @param errorType The error type needs to be set.
     */
    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public int handleLogin(String serverName, String username, String password) {
        int loginOperationId = 0;
        return handleRequest(serverName, username, password, loginOperationId);
    }

    public int handleRegistration(String serverName, String username, String password) {
        int registrationOperationId = -1;
        return handleRequest(serverName, username, password, registrationOperationId);
    }

    private int handleRequest(String serverName, String username, String password, int operationId){
        Client.setClientThread(new ClientThread(Util.isEmpty(serverName) ? "localhost" : serverName, this, username, password, operationId));
        Client.getClientThread().start();
        synchronized (this) {
            try {
//                Thread.sleep(500);
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return errorType;
    }

}
