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
    private final CollectionController collectionController;
    private final ChatController chatController;
    private final MessageController messageController;
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
        collectionController = new CollectionController();
        chatController = new ChatController();
        messageController = new MessageController();

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
            case WANT_UPDATE_CHAT_REQ:
                socketController.handleWantUpdateChatReq(rp);
                break;
            case WANT_UPDATE_CHATROOM_REQ:
                socketController.handleWantUpdateChatroomReq(rp);
                break;
            case GET_FOLDER_LIST_REQ:
                collectionController.handleGetFolderListReq(rp);
                break;
            case NEW_FOLDER_REQ:
                collectionController.handleNewFolderReq(rp);
                break;
            case DELETE_FOLDER_REQ:
                collectionController.handleDeleteFolderReq(rp);
                break;
            case GET_EDIT_FOLDER_LIST_REQ:
                collectionController.handleGetEditFolderListReq(rp);
                break;
            case SET_EDIT_FOLDER_LIST_REQ:
                collectionController.handleSetEditFolderListReq(rp);
                break;
            case GET_GROUP_LIST_REQ:
                chatController.handleGetGroupListReq(rp);
                break;
            case NEW_GROUP_REQ:
                chatController.handleNewGroupReq(rp);
                break;
            case DELETE_GROUP_REQ:
                chatController.handleDeleteGroupReq(rp);
                break;
            case GET_EDIT_GROUP_LIST_REQ:
                chatController.handleGetEditGroupListReq(rp);
                break;
            case SET_EDIT_GROUP_LIST_REQ:
                chatController.handleSetEditGroupListReq(rp);
                break;
            case GET_CHATROOM_LIST_REQ:
                chatController.handleGetChatroomListReq(rp);
                break;
            case GET_MESSAGES_REQ:
                messageController.handleGetMessagesReq(rp);
                break;
            case NEW_MESSAGE_REQ:
                messageController.handleNewMessageReq(rp);
                break;
            case DELETE_MESSAGE_REQ:
                messageController.handleDeleteMessageReq(rp);
                break;
            case GET_SELECT_LIST_REQ:
                chatController.handleGetSelectListReq(rp);
                break;
            default:
                break;
        }
    }

}