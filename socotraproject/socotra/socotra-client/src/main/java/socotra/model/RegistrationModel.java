package socotra.model;

import socotra.Client;
import socotra.util.Util;

public class RegistrationModel {
    private int errorType = 1;

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    /**
     * Send register connectionData to inform server.
     *
     * @param serverName The server name needs to connect.
     * @param username   The username used to register.
     * @param password   The password used to register.
     * @return The errorType after register.
     */
    public int handleRegister(String serverName, String username, String password) {
        int registrationOperationId = -1;
        Client.setClientThread(new ClientThread(Util.isEmpty(serverName) ? "localhost" : serverName, new LoginModel(), this, username, password,  registrationOperationId));
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
