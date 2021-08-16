package controller;

import model.Packet;

public class MainController {
    private SocketController socketController;
    private ConfigLoader configLoader;
    private DBCommunicator dbCommunicator;
    private AuthenticationController authenticationController;
    private SettingController settingController;

    public MainController() {
        configLoader = new ConfigLoader();
        dbCommunicator = new DBCommunicator();
        authenticationController = new AuthenticationController();
        settingController = new SettingController();

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
            case SETTING_INFO_REQ:
                settingController.handleSettingInfoReq(rp);
                break;
            case CHANGE_SETTING_REQ:
                settingController.handleChangeSettingReq(rp);
                break;
            case DELETE_USER_REQ:
                settingController.handleDeleteUserReq(rp);
                break;
            case LOG_OUT_REQ:
                settingController.handleLogoutReq(rp);
                break;
            default:
                break;
        }
    }

}