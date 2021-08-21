package controller;

import controller.builder.MessageBuilder;
import main.Main;
import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

import static controller.MainController.REPORTED_NUMBER_LIMIT;

public class MessageController {
    private static final MessageBuilder messageBuilder = new MessageBuilder();

    public MessageController() {
    }

    private void sendMessageToChat(Message message){
        String query = "select cm.\"memberID\", u.\"firstName\", u.\"lastName\", m.\"userID\""
                    + " from \"ChatMember\" cm, \"Message\" m, \"User\" u"
                    + " where m.\"msgID\" = " + message.getMsgID()
                    + " and m.\"chatID\" = cm.\"chatID\""
                    + " and cm.\"memberID\" = u.\"userID\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        long memberID = 0;
        String body = "";
        boolean isMine = false;
        ClientHandler clt = null;
        try {
            while(rs.next()){
                memberID = rs.getLong("memberID");
                if(Main.getMainController().getSocketController().isUserOnline(memberID)){
                    if(rs.getLong("userID")==memberID)
                        isMine = true;
                    else
                        isMine = false;

                    clt = Main.getMainController().getSocketController().getClientByID(memberID);
                    if(!isMine)
                        updateMessageStatus(message.getMsgID(), MessageStatus.RECEIVED);

                    if(clt.isWantToUpdateChat()){
                        if(!isMine)
                            updateMessageStatus(message.getMsgID(), MessageStatus.SEEN);

                        String msgImageStr = "";
                        byte[] msgImage = message.getMsgImage();
                        if(msgImage!=null)
                            msgImageStr = Base64.getEncoder().encodeToString(msgImage);

                        body = message.getMsgID() + ","
                                + PacketHandler.makeEncodedArg(message.getMsgText()) + ","
                                + msgImageStr + ","
                                + message.getMsgDateTime().toString() + ","
                                + message.isForwarded() + ","
                                + message.getMsgStatus() + ","
                                + isMine + ","
                                + PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                                + PacketHandler.makeEncodedArg(rs.getString("lastName"));
                        clt.addResponse(
                                new Packet(PacketType.GET_MESSAGES_RES,
                                        body,
                                        clt.getAuthToken(),
                                true,
                                        clt.getClientID(),
                                0)
                        );
                        sendUpdatedMessageStatusToAll(message.getChatID(),message.getMsgID(),message.getMsgStatus());
                    }
                    if(clt.isWantToUpdateChatroom()){
                        clt.addResponse(
                                new Packet(PacketType.REFRESH_CHATROOM_RES,
                                        "",
                                        clt.getAuthToken(),
                                        true,
                                        clt.getClientID(),
                                        0)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get data from DB");
        }
    }

    private void sendMessageToAll(long collectionID, String msgText, byte[] msgImage,
                                         LocalDateTime msgDateTime, long userID,
                                         boolean isForwarded){
        String query = "select u.\"userID\""
                + " from \"User\" u"
                + " where u.\"accountActive\" = true"
                + " and exists (select * from \"Relation\" r"
                + " where r.\"relationType\" = " + RelationType.FOLLOW.ordinal()
                + " and (( r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\")"
                + " or ( r.\"subjectID\" = u.\"userID\""
                + " and r.\"objectID\" = " + userID + ")))";
        ResultSet rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
        long memberID = 0;
        ResultSet rs2 = null;
        Message message = null;
        try {
            while(rs1.next()){
                memberID = rs1.getLong("userID");
                insertTwoWayChat(userID,memberID);

                query = "select c.\"chatID\""
                        + " from \"Chat\" c"
                        + " where exists (select * from\"ChatMember\" cm1"
                        + " where cm1.\"chatID\" = c.\"chatID\""
                        + " and cm1.\"memberID\" = " + memberID + ")"
                        + " and exists (select * from\"ChatMember\" cm2"
                        + " where cm2.\"chatID\" = c.\"chatID\""
                        + " and cm2.\"memberID\" = " + userID + ")"
                        + " and c.\"chatType\" = " + ChatType.TWO_WAY.ordinal();
                rs2 = Main.getMainController().getDbCommunicator().executeQuery(query);
                if(rs2.next()){
                    message = messageBuilder
                            .setMsgText(msgText)
                            .setMsgImage(msgImage)
                            .setMsgDateTime(msgDateTime)
                            .setUserID(userID)
                            .setForwarded(isForwarded)
                            .setMsgStatus(MessageStatus.SENT)
                            .build();
                    message.setChatID(rs2.getLong("chatID"));
                    insertMessage(message);
                    sendMessageToChat(message);
                }
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not get data from DB");
        }
    }

    private void sendMessageToCollection(long collectionID, String msgText, byte[] msgImage,
                                         LocalDateTime msgDateTime, long userID,
                                         boolean isForwarded){
        String query = "select cm1.\"memberID\""
                + " from \"CollectionMember\" cm1"
                + " where cm1.\"collectionID\" = " + collectionID;
        ResultSet rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            while(rs1.next()){
                insertTwoWayChat(userID,rs1.getLong("memberID"));
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
        }
        /****************************************************************************/
        query = "select cm2.\"chatID\""
                + " from \"CollectionMember\" cm1, \"ChatMember\" cm2, \"Chat\" c"
                + " where cm1.\"collectionID\" = " + collectionID
                + " and cm2.\"memberID\" = cm1.\"memberID\""
                + " and c.\"chatID\" = cm2.\"chatID\""
                + " and c.\"chatType\" = " + ChatType.TWO_WAY.ordinal();
        ResultSet rs2 = Main.getMainController().getDbCommunicator().executeQuery(query);
        Message message = null;
        try {
            while(rs2.next()){
                message = messageBuilder
                        .setMsgText(msgText)
                        .setMsgImage(msgImage)
                        .setMsgDateTime(msgDateTime)
                        .setUserID(userID)
                        .setForwarded(isForwarded)
                        .setMsgStatus(MessageStatus.SENT)
                        .build();
                message.setChatID(rs2.getLong("chatID"));
                insertMessage(message);
                sendMessageToChat(message);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        /****************************************************************************/
        query = "select *"
                + " from \"Collection\""
                + " where \"collectionID\" = " + collectionID
                + " and \"collectionName\" = 'all'";
        ResultSet rs3 = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            if(rs3.next())
                sendMessageToAll(collectionID,msgText,msgImage,msgDateTime,userID,isForwarded);
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not get data from DB");
        }
    }

    public void handleNewMessageReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",",-1);
        String msgText = PacketHandler.getDecodedArg(args[0]);
        byte[] msgImage = null;
        if(!args[1].isEmpty())
            msgImage = Base64.getDecoder().decode(args[1]);
        LocalDateTime msgDateTime = LocalDateTime.parse(args[2]);
        boolean isForwarded = Boolean.parseBoolean(args[3]);
        ArrayList<CollectionItemType> collectionItemTypes = new ArrayList<>();
        ArrayList<Long> IDs = new ArrayList<>();
        for (int i = 4; i < args.length; i++) {
            collectionItemTypes.add(CollectionItemType.valueOf(args[i]));
            IDs.add(Long.parseLong(args[i+1]));
            i++;
        }
        /****************************************************************/
        Message message = null;
        for (int i = 0; i < IDs.size(); i++) {
            if(collectionItemTypes.get(i)==CollectionItemType.FOLDER){
                sendMessageToCollection(IDs.get(i),msgText,msgImage,msgDateTime,userID,isForwarded);
                continue;
            }
            message = messageBuilder
                    .setMsgText(msgText)
                    .setMsgImage(msgImage)
                    .setMsgDateTime(msgDateTime)
                    .setUserID(userID)
                    .setForwarded(isForwarded)
                    .setMsgStatus(MessageStatus.SENT)
                    .build();
            if(collectionItemTypes.get(i)==CollectionItemType.USER){
                long twoWayChatID = insertTwoWayChat(userID,IDs.get(i));
                message.setChatID(twoWayChatID);
            }
            else if(collectionItemTypes.get(i)==CollectionItemType.CHAT){
                message.setChatID(IDs.get(i));
            }
            insertMessage(message);
            sendMessageToChat(message);
        }

        socketController.getClient(rp.getClientID())
                .addResponse(new Packet(PacketType.NEW_MESSAGE_RES,
                        "success",
                        rp.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
                );
    }

    public void handleDeleteMessageReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long msgID = Long.parseLong(rp.getBody());

        String query = "select cm.\"memberID\" from \"Message\" msg, \"ChatMember\" cm"
                + " where msg.\"msgID\" = " + msgID
                + " and msg.\"chatID\" = cm.\"chatID\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        ClientHandler clt = null;
        long memberID = 0;
        try {
            while(rs.next()){
                memberID = rs.getLong("memberID");
                if(Main.getMainController().getSocketController().isUserOnline(memberID)){
                    clt = Main.getMainController().getSocketController().getClientByID(memberID);
                    if(clt.isWantToUpdateChat()){
                        clt.addResponse(
                                new Packet(PacketType.DELETE_MESSAGE_RES,
                                        Long.toString(msgID),
                                        clt.getAuthToken(),
                                        true,
                                        clt.getClientID(),
                                        0)
                        );
                    }
                    if(clt.isWantToUpdateChatroom()){
                        clt.addResponse(
                                new Packet(PacketType.REFRESH_CHATROOM_RES,
                                        "",
                                        clt.getAuthToken(),
                                        true,
                                        clt.getClientID(),
                                        0)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not insert data to DB");
        }

        deleteMessage(msgID);
    }

    public void handleGetMessagesReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",", -1);
        int maxNum = Integer.parseInt(args[0]);
        LocalDateTime lastMsgDateTime = null;
        if(!args[1].isEmpty())
            lastMsgDateTime = LocalDateTime.parse(args[1]);
        long chatID = Long.parseLong(args[2]);
        /***************************************************************************************/
        setReceivedMessagesToSeen(chatID,userID);

        String query = "select m.*, u.\"firstName\", u.\"lastName\""
                + " from \"Message\" m, \"User\" u"
                + " where m.\"chatID\" = " + chatID;
        if(lastMsgDateTime !=null) {
            query += (" and m.\"msgDateTime\" < '"
                    + lastMsgDateTime.toLocalDate().toString()
                    + "T" + lastMsgDateTime.toLocalTime().toString() + "'");
        }
        query += (" and m.\"userID\" = u.\"userID\" "
                + "order by m.\"msgDateTime\" desc "
                + "limit " + maxNum);
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            boolean isMine = false;
            while(rs.next()){
                if(rs.getLong("userID")==userID)
                    isMine = true;
                else
                    isMine = false;
                /************************************/
                String msgImageStr = "";
                byte[] msgImage = rs.getBytes("msgImage");
                if(msgImage!=null)
                    msgImageStr = Base64.getEncoder().encodeToString(msgImage);

                body = rs.getLong("msgID") + ","
                        + PacketHandler.makeEncodedArg(rs.getString("msgText")) + ","
                        + msgImageStr + ","
                        + rs.getTimestamp("msgDateTime").toLocalDateTime().toString() + ","
                        + rs.getBoolean("forwarded") + ","
                        + MessageStatus.values()[rs.getInt("msgStatus")] + ","
                        + isMine + ","
                        + PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("lastName"));
                clt.addResponse(
                        new Packet(PacketType.GET_MESSAGES_RES,
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

    public void handleEditMessageReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String[] args = rp.getBody().split(",",-1);

        long msgID = Long.parseLong(args[0]);
        String body = "error,0,";
        String msgText = "";
        if(!args[1].isEmpty()) {
            msgText = PacketHandler.getDecodedArg(args[1]);
            String query = "update \"Message\" set \"msgText\" = "
                    + "'" + msgText + "'"
                    + " where \"msgID\" = " + msgID;
            if(Main.getMainController().getDbCommunicator().executeUpdate(query)!=0)
                body = "success," + msgID + "," + PacketHandler.makeEncodedArg(msgText);
        }
        socketController.getClient(rp.getClientID()).addResponse(
                new Packet(PacketType.EDIT_MESSAGE_RES,
                        body,
                        rp.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }

    private long insertTwoWayChat(long userID1, long userID2){
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
        String chatName = "";
        try {
            if(rs.next()){
                chatID = rs.getLong("chatID");
            }
            else{
                query = "select \"firstName\", \"lastName\""
                        + " from \"User\""
                        + " where \"userID\" = " + userID1
                        + " or \"userID\" = " + userID2;
                ResultSet rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
                rs1.next(); chatName = rs1.getString("firstName") + " " + rs1.getString("lastName");
                chatName += " | ";
                rs1.next(); chatName += rs1.getString("firstName") + " " + rs1.getString("lastName");
                chatID = ChatController.insertChat(chatName,ChatType.TWO_WAY);
                ChatController.insertChatMember(chatID,userID1);
                ChatController.insertChatMember(chatID,userID2);
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not insert data to DB");
        }
        return chatID;
    }

    public static void updateMessageStatus(long msgID,MessageStatus messageStatus){
        String query = "update \"Message\" set \"msgStatus\" = "
                + messageStatus.ordinal()
                + " where \"msgID\" = " + msgID;
        Main.getMainController().getDbCommunicator().executeUpdate(query);
    }

    public static boolean insertMessage(Message msg){
        String msgStatus = "null";
        if(msg.getMsgStatus()!=null)
            msgStatus = Integer.toString(msg.getMsgStatus().ordinal());
        String query = "insert into \"Message\" ("
                + "\"userID\","
                + "\"chatID\","
                + "\"msgID\","
                + "\"msgText\","
                + "\"msgDateTime\","
                + "\"forwarded\","
                + "\"msgStatus\")"
                + " values ("
                + msg.getUserID() + ","
                + msg.getChatID() + ","
                + msg.getMsgID() + ","
                + "'" + msg.getMsgText() + "',"
                + "'" + msg.getMsgDateTime().toLocalDate().toString() + "T"
                + msg.getMsgDateTime().toLocalTime().toString() + "'" + ","
                + msg.isForwarded() + ","
                + msgStatus + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(msg.getMsgImage()!=null) {
            query = "update \"Message\" set \"msgImage\" = ? where \"msgID\" = " + msg.getMsgID();
            int updatedImageNum = Main.getMainController().getDbCommunicator().executeUpdateBytea(query, msg.getMsgImage());
        }

        if(updatedRowsNum==1){
            LogHandler.logger.info("msgID:" + msg.getMsgID() + " created");
            return true;
        }
        else{
            LogHandler.logger.error("could not insert message in DB");
            return false;
        }
    }

    public static boolean deleteMessage(long msgID){
        String query = "delete from \"Message\" where"
                + " \"msgID\" = " + msgID;
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        if(updatedRowsNum==1){
            LogHandler.logger.info("msgID:" + msgID + " removed");
            return true;
        }
        else{
            LogHandler.logger.error("could not remove Message from DB");
            return false;
        }
    }

    public static void setSentMessagesToReceived(long userID){
        String query = "select m.\"msgID\", m.\"chatID\" from \"Message\" m, \"ChatMember\" cm"
                    + " where m.\"chatID\" = cm.\"chatID\""
                    + " and cm.\"memberID\" = " + userID
                    + " and m.\"userID\" != " + userID
                    + " and m.\"msgStatus\" = " + MessageStatus.SENT.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);

        long msgID=0, chatID=0;
        try {
            while (rs.next()){
                msgID = rs.getLong("msgID");
                chatID = rs.getLong("chatID");
                updateMessageStatus(msgID,MessageStatus.RECEIVED);
                sendUpdatedMessageStatusToAll(chatID,msgID,MessageStatus.RECEIVED);
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not update message in DB");
        }
    }

    public static void setReceivedMessagesToSeen(long chatID,long userID){
        String query = "select m.\"msgID\" from \"Message\" m"
                + " where m.\"chatID\" = " + chatID
                + " and m.\"userID\" != " + userID
                + " and m.\"msgStatus\" = " + MessageStatus.RECEIVED.ordinal();
        ResultSet rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);

        long msgID = 0;
        try {
            while (rs1.next()){
                msgID = rs1.getLong("msgID");
                updateMessageStatus(msgID,MessageStatus.SEEN);
                sendUpdatedMessageStatusToAll(chatID,msgID,MessageStatus.SEEN);
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not update message in DB");
        }
    }

    private static void sendUpdatedMessageStatusToAll(long chatID, long msgID, MessageStatus messageStatus){
        SocketController socketController = Main.getMainController().getSocketController();
        String query = "select \"memberID\" from \"ChatMember\""
                + " where \"chatID\" = " + chatID;
        ResultSet rs2 = Main.getMainController().getDbCommunicator().executeQuery(query);
        long memberID = 0;
        ClientHandler clt = null;
        try {
            while (rs2.next()){
                memberID = rs2.getLong("memberID");
                if(socketController.isUserOnline(memberID)){
                    clt = socketController.getClientByID(memberID);
                    if(clt.isWantToUpdateChat()){
                        clt.addResponse(
                                new Packet(PacketType.MESSAGE_CHANGE_STATUS_RES,
                                        msgID + "," + messageStatus,
                                        clt.getAuthToken(),
                                        true,
                                        clt.getClientID(),
                                        0)
                        );
                    }
                }
            }
        } catch (SQLException throwables) {
            //throwables.printStackTrace();
            LogHandler.logger.error("could not get data from DB");
        }
    }
}
