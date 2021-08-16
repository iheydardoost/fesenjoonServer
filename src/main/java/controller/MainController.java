package controller;

import main.LoopHandler;
import model.Packet;

public class MainController {
    private SocketController socketController;
    private ConfigLoader configLoader;
    private DBCommunicator dbCommunicator;
    private AuthenticationController authenticationController;

    public MainController() {
        configLoader = new ConfigLoader();
        dbCommunicator = new DBCommunicator();
        authenticationController = new AuthenticationController();

        socketController = new SocketController();
    }

    public SocketController getSocketController() {
        return socketController;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public DBCommunicator getDbCommunicator() {
        return dbCommunicator;
    }

    public void handleRequest(Packet rp){
        switch (rp.getPacketType()){
            case SIGN_UP_REQ:
                authenticationController.handleSignUp(rp);
                break;
            case LOG_IN_REQ:
                authenticationController.handleLogIn(rp);
                break;
            default:
                break;
        }
    }

}