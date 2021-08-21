package controller;

import controller.builder.ChatBuilder;
import main.Main;
import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

public class ChatController {
    private static final ChatBuilder chatBuilder = new ChatBuilder();

    public ChatController() {
    }

    public void handleGetGroupListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select c.* from \"Chat\" c, \"ChatMember\" cm"
                + " where cm.\"memberID\" = " + userID
                + " and c.\"chatID\" = cm.\"chatID\""
                + " and c.\"chatType\" != " + ChatType.TWO_WAY.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            while(rs.next()){
                body = rs.getLong("chatID") + ","
                        + PacketHandler.makeEncodedArg(rs.getString("chatName"));
                clt.addResponse(
                        new Packet(PacketType.GET_GROUP_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleNewGroupReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String body = "error";
        long chatID = insertChat(PacketHandler.getDecodedArg(rp.getBody()),ChatType.GROUP);
        if(chatID!=0) {
            body = "success";
            insertChatMember(chatID, userID);
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.NEW_GROUP_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleDeleteGroupReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String body = "error";
        if(deleteChat(Long.parseLong(rp.getBody())))
            body = "success";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.DELETE_GROUP_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetEditGroupListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        long chatID = Long.parseLong(rp.getBody());

        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userID\""
                + " from \"User\" u"
                + " where u.\"accountActive\" = true"
                + " and exists (select * from \"Relation\" r"
                + " where r.\"relationType\" = " + RelationType.FOLLOW.ordinal()
                + " and (( r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\")"
                + " or ( r.\"subjectID\" = u.\"userID\""
                + " and r.\"objectID\" = " + userID + ")))";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            boolean isSelected = false;
            while(rs.next()){
                isSelected = isMemberInChat(rs.getLong("userID"),chatID);
                body =  PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("lastName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("userName")) + ","
                        + isSelected;
                clt.addResponse(
                        new Packet(PacketType.GET_EDIT_GROUP_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleSetEditGroupListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String[] args = rp.getBody().split(",",-1);
        long chatID = Long.parseLong(args[0]);

        String query = "delete from \"ChatMember\" where"
                + " \"chatID\" = " + chatID;
        Main.getMainController().getDbCommunicator().executeUpdate(query);
        /*********************************************************************/
        int updatedRowsNum = 0;
        long memberID = 0;
        for (int i = 1; i < args.length; i++) {
            memberID = SettingController.findUserID(PacketHandler.getDecodedArg(args[i]));
            if(memberID!=0)
                if(insertChatMember(chatID,memberID))
                    updatedRowsNum++;
        }

        String body = "";
        if(updatedRowsNum==args.length-1){
            body = "success";
            insertChatMember(chatID,userID);
            LogHandler.logger.info("chatID:" + chatID + " members added");
        }
        else{
            body = "error";
            LogHandler.logger.error("could not add chat member in DB");
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.SET_EDIT_GROUP_LIST_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetChatroomListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select c.* from \"Chat\" c, \"ChatMember\" cm"
                + " where cm.\"memberID\" = " + userID
                + " and c.\"chatID\" = cm.\"chatID\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            int unSeen = 0;
            ChatType chatType;
            long chatID = 0;
            while(rs.next()){
                chatType = ChatType.values()[rs.getInt("chatType")];
                chatID = rs.getLong("chatID");
                if(chatType==ChatType.SAVED_MESSAGES)
                    unSeen = 0;
                else
                    unSeen = getChatUnseen(userID,chatID);
                body =  chatID + ","
                        + PacketHandler.makeEncodedArg(rs.getString("chatName")) + ","
                        + unSeen;
                clt.addResponse(
                        new Packet(PacketType.GET_CHATROOM_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleGetSelectListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select u.\"firstName\", u.\"lastName\", u.\"userID\""
                + " from \"User\" u"
                + " where u.\"accountActive\" = true"
                + " and exists (select * from \"Relation\" r"
                + " where r.\"relationType\" = " + RelationType.FOLLOW.ordinal()
                + " and (( r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\")"
                + " or ( r.\"subjectID\" = u.\"userID\""
                + " and r.\"objectID\" = " + userID + ")))";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            while(rs.next()){
                body =  PacketHandler.makeEncodedArg(rs.getString("firstName") + " " + rs.getString("lastName")) + ","
                        + CollectionItemType.USER + ","
                        + rs.getLong("userID");
                clt.addResponse(
                        new Packet(PacketType.GET_SELECT_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        /***********************************************************************/
        query = "select *"
                + " from \"Collection\""
                + " where \"ownerID\" = " + userID;
        rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            while(rs.next()){
                body =  PacketHandler.makeEncodedArg(rs.getString("collectionName")) + ","
                        + CollectionItemType.FOLDER + ","
                        + rs.getLong("collectionID");
                clt.addResponse(
                        new Packet(PacketType.GET_SELECT_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        /***********************************************************************/
        query = "select * from \"Chat\" c, \"ChatMember\" cm"
                + " where c.\"chatType\" != " + ChatType.TWO_WAY.ordinal()
                + " and cm.\"memberID\" = " + userID
                + " and cm.\"chatID\" = c.\"chatID\"";
        rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            while(rs.next()){
                body =  PacketHandler.makeEncodedArg(rs.getString("chatName")) + ","
                        + CollectionItemType.CHAT + ","
                        + rs.getLong("chatID");
                clt.addResponse(
                        new Packet(PacketType.GET_SELECT_LIST_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleGetChatIDReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID1 = socketController.getClient(rp.getClientID()).getUserID();
        long userID2 = Long.parseLong(rp.getBody());

        String query = "select c.\"chatID\""
                + " from \"ChatMember\" cm, \"Chat\" c"
                + " where c.\"chatType\" = " + ChatType.TWO_WAY.ordinal()
                + " and exists (select * from \"ChatMember\" cm1"
                + " where cm1.\"chatID\" = c.\"chatID\""
                + " and cm1.\"memberID\" = " + userID1 + ")"
                + " and exists (select * from \"ChatMember\" cm2"
                + " where cm2.\"chatID\" = c.\"chatID\""
                + " and cm2.\"memberID\" = " + userID2 + ")";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        long chatID = 0;
        try {
            if(rs.next())
                chatID = rs.getLong("chatID");
            socketController.getClient(rp.getClientID()).addResponse(
                    new Packet(PacketType.GET_CHAT_ID_BY_USER_ID_RES,
                            Long.toString(chatID),
                            rp.getAuthToken(),
                            true,
                            rp.getClientID(),
                            rp.getRequestID())
            );
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    public static int getChatUnseen(long userID, long chatID){
        String query = "select count(*) from \"Message\""
                + " where \"chatID\" = " + chatID
                + " and \"userID\" != " + userID
                + " and \"msgStatus\" != " + MessageStatus.SEEN.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    public static boolean isMemberInChat(long memberID,long chatID){
        String query = "select count(*) from \"ChatMember\""
                + " where \"chatID\" = " + chatID
                + " and \"memberID\" = " + memberID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            if(rs.getInt(1)!=0)
                return true;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public static long insertChat(String name, ChatType chatType){
        Chat chat = chatBuilder
                .setChatName(name)
                .setChatType(chatType)
                .build();
        String query = "insert into \"Chat\" ("
                + "\"chatID\","
                + "\"chatName\","
                + "\"chatType\")"
                + " values ("
                + chat.getChatID() + ","
                + "'" + chat.getChatName() + "'" + ","
                + chat.getChatType().ordinal() + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("chatID:" + chat.getChatID()
                    + " chatName:" + chat.getChatName()
                    + " created");
            return chat.getChatID();
        }
        else{
            LogHandler.logger.error("could not save chat in DB");
            return 0;
        }
    }

    public static boolean deleteChat(long chatID){
        String query = "delete from \"Chat\" where"
                + " \"chatID\" = " + chatID
                + " and \"chatType\" != " + ChatType.SAVED_MESSAGES.ordinal();
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        if(updatedRowsNum==1){
            LogHandler.logger.info("chatID:" + chatID + " deleted");
            return true;
        }
        else{
            LogHandler.logger.error("could not delete chat from DB");
            return false;
        }
    }

    public static boolean insertChatMember(long chatID, long memberID){
        String query = "insert into \"ChatMember\" ("
                + "\"memberID\","
                + "\"chatID\")"
                + " select "
                + memberID + ","
                + chatID
                + " where exists (select * from \"Chat\" c"
                + " where c.\"chatID\" = " + chatID + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("chatID:" + chatID
                    + " memberID:" + memberID
                    + " added");
            return true;
        }
        else{
            LogHandler.logger.error("could not add chat member in DB");
            return false;
        }
    }

    public static boolean deleteChatMember(long chatID, long memberID){
        String query = "delete from \"ChatMember\" where"
                + " \"chatID\" = " + chatID
                + " and \"memberID\" = " + memberID;
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        if(updatedRowsNum==1){
            LogHandler.logger.info("chatID:" + chatID
                    + " memberID:" + memberID
                    + " removed");
            return true;
        }
        else{
            LogHandler.logger.error("could not remove Chat member from DB");
            return false;
        }
    }
}
