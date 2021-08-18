package controller;

import model.Packet;

public class MainController {
    private SocketController socketController;
    private ConfigLoader configLoader;
    private DBCommunicator dbCommunicator;
    private AuthenticationController authenticationController;
    private SettingController settingController;
    private TimelineController timelineController;
    private ExplorerController explorerController;
    private TweetController tweetController;
    private RelationController relationController;
    public static final int REPORTED_NUMBER_LIMIT = 3;

    public MainController() {
        configLoader = new ConfigLoader();
        dbCommunicator = new DBCommunicator();
        authenticationController = new AuthenticationController();
        settingController = new SettingController();
        timelineController = new TimelineController();
        explorerController = new ExplorerController();
        tweetController = new TweetController();
        relationController = new RelationController();

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
            case TIMELINE_TWEET_REQ:
                timelineController.handleTimelineTweetReq(rp);
                break;
            case EXPLORER_TWEET_REQ:
                explorerController.handleExplorerTweetReq(rp);
                break;
            case NEW_TWEET_REQ:
                tweetController.handleNewTweetReq(rp);
                break;
            case REPORT_TWEET_REQ:
                tweetController.handleReportTweetReq(rp);
                break;
            case LIKE_TWEET_REQ:
                tweetController.handleLikeTweetReq(rp);
                break;
            case REPORT_USER_REQ:
                relationController.handleReportUserReq(rp);
                break;
            case MUTE_USER_REQ:
                relationController.handleMuteUserReq(rp);
                break;
            case UNMUTE_USER_REQ:
                relationController.handleUnmuteUserReq(rp);
                break;
            case BLOCK_USER_REQ:
                relationController.handleBlockUserReq(rp);
                break;
            case UNBLOCK_USER_REQ:
                relationController.handleUnblockUserReq(rp);
                break;
            case GET_TWEET_REQ:
                tweetController.handleGetTweetReq(rp);
                break;
            case GET_COMMENTS_REQ:
                tweetController.handleGetCommentsReq(rp);
                break;
            default:
                break;
        }
    }

}