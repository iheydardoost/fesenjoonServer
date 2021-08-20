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

    public void handleNewMessageReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",",-1);
        String msgText = args[0];
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
                    .build();
            message.setChatID(IDs.get(i));
            if(collectionItemTypes.get(i)==CollectionItemType.USER){
                message.setMsgStatus(MessageStatus.SENT);
            }
            else if(collectionItemTypes.get(i)==CollectionItemType.CHAT){
                message.setMsgStatus(null);
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
                    clt = Main.getMainController().getSocketController().getClientByID(memberID);
                    if(clt.isWantToUpdateChat()){
                        updateMessageStatus(message.getMsgID(),MessageStatus.SEEN);

                        String msgImageStr = "";
                        byte[] msgImage = message.getMsgImage();
                        if(msgImage!=null)
                            msgImageStr = Base64.getEncoder().encodeToString(msgImage);

                        if(rs.getLong("userID")==memberID)
                            isMine = true;
                        else
                            isMine = false;

                        body = message.getMsgID() + ","
                                + message.getMsgText() + ","
                                + msgImageStr + ","
                                + message.getMsgDateTime().toString() + ","
                                + message.isForwarded() + ","
                                + message.getMsgStatus() + ","
                                + isMine + ","
                                + rs.getString("firstName") + ","
                                + rs.getString("lastName");
                        clt.addResponse(
                                new Packet(PacketType.GET_MESSAGES_RES,
                                "success",
                                        clt.getAuthToken(),
                                true,
                                        clt.getClientID(),
                                0)
                        );
                    }
                    if(clt.isWantToUpdateChatroom()){
                        updateMessageStatus(message.getMsgID(),MessageStatus.RECEIVED);
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
        }
    }

    private void sendMessageToCollection(long collectionID, String msgText, byte[] msgImage,
                                         LocalDateTime msgDateTime, long userID,
                                         boolean isForwarded){
        String query = "select cm2.\"chatID\" from \"CollectionMember\" cm1, \"ChatMember\" cm2"
                    + " where cm1.\"collectionID\" = " + collectionID
                    + " and cm2.\"memberID\" = cm1.\"memberID\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        Message message = null;
        try {
            while(rs.next()){
                message = messageBuilder
                        .setMsgText(msgText)
                        .setMsgImage(msgImage)
                        .setMsgDateTime(msgDateTime)
                        .setUserID(userID)
                        .setForwarded(isForwarded)
                        .setMsgStatus(MessageStatus.SENT)
                        .build();
                message.setChatID(rs.getLong("chatID"));
                insertMessage(message);
                sendMessageToChat(message);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
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
        String query = "select m.*, u.\"firstName\", u.\"lastName\""
                + " from \"Message\" m, \"User\" u"
                + " where m.\"chatID\" = " + chatID;
        if(lastMsgDateTime !=null) {
            query += (" and m.\"msgDateTime\" < '"
                    + lastMsgDateTime.toLocalDate().toString()
                    + "T" + lastMsgDateTime.toLocalTime().toString() + "'");
        }
        query += (" and m.\"userID\" = u.\"userID\" "
                + "order by t.\"msgDateTime\" desc "
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
                        + rs.getString("msgText") + ","
                        + msgImageStr + ","
                        + rs.getTimestamp("msgDateTime").toLocalDateTime().toString() + ","
                        + rs.getBoolean("forwarded") + ","
                        + MessageStatus.values()[rs.getInt("msgStatus")] + ","
                        + isMine + ","
                        + rs.getString("firstName") + ","
                        + rs.getString("lastName");
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
        String body = "error";
        if(!args[1].isEmpty()) {
            String query = "update \"Message\" set \"msgText\" = "
                    + "'" + args[1] + "'"
                    + " where \"msgID\" = " + msgID;
            if(Main.getMainController().getDbCommunicator().executeUpdate(query)!=0)
                body = "success";
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

    public static void updateMessageStatus(long msgID,MessageStatus messageStatus){
        String query = "update \"Message\" set \"msgStatus\" = "
                + messageStatus.ordinal()
                + " where \"msgID\" = " + msgID;
        Main.getMainController().getDbCommunicator().executeUpdate(query);
    }

    public static boolean insertMessage(Message msg){
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
                + msg.getMsgStatus().ordinal() + ")";
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
}
