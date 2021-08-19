package controller;

import model.Packet;

public class MainController {
    private final SocketController socketController;
    private final ConfigLoader configLoader;
    private final DBCommunicator dbCommunicator;
    private final AuthenticationController authenticationController;
    private final SettingController settingController;
    private final TimelineController timelineController;
    private final ExplorerController explorerController;
    private final TweetController tweetController;
    private final RelationController relationController;
    private final PrivateController privateController;
    private final NotificationController notificationController;
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
        privateController = new PrivateController();
        notificationController = new NotificationController();

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
            case SEARCH_USERNAME_REQ:
                explorerController.handleSearchUsernameReq(rp);
                break;
            case GET_TWEET_LIST_REQ:
                tweetController.handleGetTweetListReq(rp);
                break;
            case GET_BLACK_LIST_REQ:
                relationController.handleGetBlackListReq(rp);
                break;
            case GET_FOLLOWERS_LIST_REQ:
                relationController.handleGetFollowersListReq(rp);
                break;
            case GET_FOLLOWINGS_LIST_REQ:
                relationController.handleGetFollowingsListReq(rp);
                break;
            case GET_NOTIFICATIONS_REQ:
                notificationController.handleGetNotificationReq(rp);
                break;
            case GET_PENDING_FOLLOW_REQ:
                notificationController.handleGetPendingFollowReq(rp);
                break;
            case ACCEPT_FOLLOW_REQ:
                notificationController.handleAcceptFollowReq(rp);
                break;
            case REJECT_FOLLOW_REQ:
                notificationController.handleRejectFollowReq(rp);
                break;
            case EDIT_USER_INFO_REQ:
                privateController.handleEditUserInfoReq(rp);
                break;
            case GET_USER_INFO_REQ:
                privateController.handleGetUserInfo(rp);
                break;
            case GET_PRIVATE_INFO_REQ:
                privateController.handleGetPrivateInfo(rp);
                break;
            case GET_EDIT_INFO_REQ:
                privateController.handleGetEditInfo(rp);
                break;
            case FOLLOW_USER_REQ:
                relationController.handleFollowUserReq(rp);
                break;
            case UNFOLLOW_USER_REQ:
                relationController.handleUnfollowUserReq(rp);
                break;
            default:
                break;
        }
    }

}