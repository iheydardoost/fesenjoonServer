package controller;

import main.Main;
import model.NotificationType;
import model.Packet;
import model.PacketType;
import model.RelationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class NotificationController {

    public void handleGetNotificationReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        boolean isSystem = false;
        if(rp.getBody().equals("system"))
            isSystem = true;
        /***************************************************************************************/
        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\", n.\"notificationType\""
                + " from \"User\" u, \"Notification\" n"
                + " where n.\"objectID\" = " + userID
                + " and n.\"subjectID\" = u.\"userID\"";
        if(isSystem) {
             query += " and (n.\"notificationType\" = " + NotificationType.FOLLOWED.ordinal()
                    + " or n.\"notificationType\" = " + NotificationType.UNFOLLOWED.ordinal() + ")";
        }
        else{
            query += " and n.\"notificationType\" = " + NotificationType.FOLLOW_REQUEST.ordinal();
        }
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendNotificationList(rs,rp,PacketType.GET_NOTIFICATIONS_RES);
        if(isSystem){
            query = "delete from \"Notification\" n where"
                    + " n.\"objectID\" = " + userID
                    + " and (n.\"notificationType\" = " + NotificationType.FOLLOWED.ordinal()
                    + " or n.\"notificationType\" = " + NotificationType.UNFOLLOWED.ordinal() + ")";
            Main.getMainController().getDbCommunicator().executeUpdate(query);
        }
    }

    public void handleGetPendingFollowReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        /***************************************************************************************/
        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\", n.\"notificationType\""
                + " from \"User\" u, \"Notification\" n"
                + " where n.\"subjectID\" = " + userID
                + " and n.\"objectID\" = u.\"userID\""
                + " and (n.\"notificationType\" = " + NotificationType.FOLLOW_REQUEST.ordinal()
                + " or n.\"notificationType\" = " + NotificationType.ACCEPTED.ordinal()
                + " or n.\"notificationType\" = " + NotificationType.REJECTED.ordinal() + ")";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendNotificationList(rs,rp,PacketType.GET_PENDING_FOLLOW_RES);
        query = "delete from \"Notification\" n where"
                + " n.\"subjectID\" = " + userID
                + " and (n.\"notificationType\" = " + NotificationType.ACCEPTED.ordinal()
                + " or n.\"notificationType\" = " + NotificationType.REJECTED.ordinal() + ")";
        Main.getMainController().getDbCommunicator().executeUpdate(query);
    }

    public void handleAcceptFollowReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long objectID = socketController.getClient(rp.getClientID()).getUserID();
        long subjectID = 0;
        String userName = rp.getBody();

        String query = "select u.\"userID\" from \"User\" u"
                + " where u.\"userName\" = " + "'" + userName + "'"
                + " and u.\"accountActive\" = true";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        String body = "";
        try {
            rs.next();
            subjectID = rs.getLong("userID");
            deleteNotification(subjectID,objectID,NotificationType.FOLLOW_REQUEST);
            insertNotification(subjectID,objectID,NotificationType.FOLLOWED);
            insertNotification(subjectID,objectID,NotificationType.ACCEPTED);
            RelationController.insertRelation(subjectID,objectID,RelationType.FOLLOW);
            body = "success";
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
            body = "error";
        }

        ClientHandler clt = socketController.getClient(rp.getClientID());
        clt.addResponse(
                new Packet(PacketType.ACCEPT_FOLLOW_RES,
                        body,
                        clt.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }

    public void handleRejectFollowReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long objectID = socketController.getClient(rp.getClientID()).getUserID();
        long subjectID = 0;
        String[] args = rp.getBody().split(",",-1);
        String userName = args[0];

        String query = "select u.\"userID\" from \"User\" u"
                + " where u.\"userName\" = " + "'" + userName + "'"
                + " and u.\"accountActive\" = true";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        String body = "";
        try {
            rs.next();
            subjectID = rs.getLong("userID");
            deleteNotification(subjectID,objectID,NotificationType.FOLLOW_REQUEST);
            if(args[1].equals("yes"))
                insertNotification(subjectID,objectID,NotificationType.REJECTED);
            body = "success";
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
            body = "error";
        }

        ClientHandler clt = socketController.getClient(rp.getClientID());
        clt.addResponse(
                new Packet(PacketType.REJECT_FOLLOW_RES,
                        body,
                        clt.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }

    public static void sendNotificationList(ResultSet rs, Packet request, PacketType packetType){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(request.getClientID()).getUserID();

        String message = "";

        try {
            ClientHandler clt = socketController.getClient(request.getClientID());
            String body = "";
            while(rs.next()){
                String userImageStr = "";
                byte[] userImage = rs.getBytes("userImage");
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);

                NotificationType notificationType = NotificationType.values()[rs.getInt("notificationType")];
                if(notificationType==NotificationType.FOLLOWED)
                    message = "followed you";
                else if(notificationType==NotificationType.UNFOLLOWED)
                    message = "unfollowed you";
                else if(packetType == PacketType.GET_PENDING_FOLLOW_RES) {
                    if(notificationType==NotificationType.ACCEPTED)
                        message = "accepted";
                    else if(notificationType==NotificationType.REJECTED)
                        message = "rejected";
                    else if(notificationType==NotificationType.FOLLOW_REQUEST)
                        message = "pending";
                }

                body = rs.getString("userName") + ","
                        + rs.getString("firstName") + ","
                        + rs.getString("lastName") + ","
                        + userImageStr + ","
                        + message;
                clt.addResponse(
                        new Packet(packetType,
                                body,
                                clt.getAuthToken(),
                                true,
                                request.getClientID(),
                                request.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public static boolean insertNotification(long subjectID, long objectID, NotificationType notificationType){
        String query = "select count(*) from \"Notification\" where"
                + " \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"notificationType\" = " + notificationType.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        int rowsExisted=0;
        try {
            rs.next();
            rowsExisted = rs.getInt(1);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        if(rowsExisted!=0){
            return false;
        }
        /************************************************************************/
        query = "insert into \"Notification\" ("
                + "\"subjectID\","
                + "\"objectID\","
                + "\"notificationType\")"
                + " values ("
                + subjectID + ","
                + objectID + ","
                + notificationType.ordinal() + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("subjectID:" + subjectID + " " + notificationType + " objectID:" + objectID);
            return true;
        }
        else{
            LogHandler.logger.error("could not save Notification in DB");
            return false;
        }
    }

    public static boolean deleteNotification(long subjectID, long objectID, NotificationType notificationType){
        String query = "delete from \"Notification\" where"
                + " \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"notificationType\" = " + notificationType.ordinal();
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("subjectID:" + subjectID + " " + notificationType + " objectID:" + objectID + " deleted");
            return true;
        }
        else{
            LogHandler.logger.error("could not delete Notification from DB");
            return false;
        }
    }
}
