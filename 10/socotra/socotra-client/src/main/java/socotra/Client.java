package socotra;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import socotra.controller.HomeController;
import socotra.controller.LoginRegistrationController;
import socotra.model.ClientThread;
import socotra.model.HomeModel;
import socotra.model.LoginRegistrationModel;
import socotra.util.SetOnlineUsers;

public class Client extends Application {

    /**
     * The stage to show in the screen.
     */
    private static Stage stage;

    // Put all the controllers and models in Client to manage.

    private static LoginRegistrationController loginRegistrationController;
    private static LoginRegistrationModel loginRegistrationModel;
    private static HomeController homeController;
    private static HomeModel homeModel;
    private static ClientThread clientThread;
    private static SetOnlineUsers setOnlineUsers;

    public static LoginRegistrationController getLoginRegistrationController() {
        return loginRegistrationController;
    }

    public static void setLoginRegistrationController(LoginRegistrationController loginRegistrationController) {
        Client.loginRegistrationController = loginRegistrationController;
    }

    public static LoginRegistrationModel getLoginRegistrationModel() {
        return loginRegistrationModel;
    }

    public static void setLoginRegistrationModel(LoginRegistrationModel loginRegistrationModel) {
        Client.loginRegistrationModel = loginRegistrationModel;
    }

    public static HomeController getHomeController() {
        return homeController;
    }

    public static void setHomeController(HomeController homeController) {
        Client.homeController = homeController;
    }

    public static HomeModel getHomeModel() {
        return homeModel;
    }

    public static void setHomeModel(HomeModel homeModel) {
        Client.homeModel = homeModel;
        if (setOnlineUsers != null) {
            synchronized (setOnlineUsers) {
                setOnlineUsers.notify();
            }
        }
    }

    public static ClientThread getClientThread() {
        return clientThread;
    }

    public static void setClientThread(ClientThread clientThread) {
        Client.clientThread = clientThread;
    }

    public static void setSetOnlineUsers(SetOnlineUsers setOnlineUsers) {
        Client.setOnlineUsers = setOnlineUsers;
    }

    /**
     * The program entry.
     *
     * @param args The arguments of the entry.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Set the scene of the stage.
     *
     * @param scene The scene needs to be shown.
     */
    public static void setScene(Scene scene) {
        stage.setScene(scene);
    }

    /**
     * Start to show the GUI.
     *
     * @param primaryStage The stage to show in the screen.
     * @throws Exception The exception thrown by javafx program.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load .fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Pane tempPane = loader.load();
        setLoginRegistrationController(loader.getController());
        // Create scene
        Scene tempScene = new Scene(tempPane);
        // Add scene to the stage
        primaryStage.setScene(tempScene);
        // Show the stage
        primaryStage.show();
        stage = primaryStage;
    }
}
