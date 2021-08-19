package controller;

import main.Main;
import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class RelationController {

    public void handleReportUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        String body = "";
        if(insertRelation(socketController.getClient(rp.getClientID()).getUserID(),
                Long.parseLong(rp.getBody()),
                RelationType.REPORT))
            body = "success";
        else
            body = "error";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.REPORT_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleMuteUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        String body = "";
        if(insertRelation(socketController.getClient(rp.getClientID()).getUserID(),
                Long.parseLong(rp.getBody()),
                RelationType.MUTE))
            body = "success";
        else
            body = "error";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.MUTE_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleUnmuteUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        String body = "";
        if(deleteRelation(socketController.getClient(rp.getClientID()).getUserID(),
                Long.parseLong(rp.getBody()),
                RelationType.MUTE))
            body = "success";
        else
            body = "error";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.UNMUTE_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleBlockUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        long objectID = Long.parseLong(rp.getBody());
        String body = "";
        if(insertRelation(subjectID, objectID, RelationType.BLOCK)) {
            deleteRelation(subjectID, objectID, RelationType.FOLLOW);
            deleteRelation(objectID, subjectID, RelationType.FOLLOW);
            body = "success";
            NotificationController.insertNotification(subjectID,objectID,NotificationType.UNFOLLOWED);
            NotificationController.deleteNotification(objectID,subjectID,NotificationType.FOLLOW_REQUEST);
            NotificationController.deleteNotification(subjectID,objectID,NotificationType.FOLLOW_REQUEST);
        }
        else {
            body = "error";
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.BLOCK_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleUnblockUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        String body = "";
        if(deleteRelation(
                socketController.getClient(rp.getClientID()).getUserID(),
                Long.parseLong(rp.getBody()),
                RelationType.BLOCK))
            body = "success";
        else
            body = "error";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.UNBLOCK_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetBlackListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        /***************************************************************************************/
        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"User\" u, \"Relation\" r"
                + " where r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\""
                + " and r.\"relationType\" = " + RelationType.BLOCK.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendRelationList(rs,rp,PacketType.GET_BLACK_LIST_RES);
    }

    public void handleGetFollowersListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        /***************************************************************************************/
        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"User\" u, \"Relation\" r"
                + " where r.\"objectID\" = " + userID
                + " and r.\"subjectID\" = u.\"userID\""
                + " and r.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendRelationList(rs,rp,PacketType.GET_FOLLOWERS_LIST_RES);
    }

    public void handleGetFollowingsListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        /***************************************************************************************/
        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"User\" u, \"Relation\" r"
                + " where r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\""
                + " and r.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendRelationList(rs,rp,PacketType.GET_FOLLOWINGS_LIST_RES);
    }

    public void handleFollowUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        ClientHandler clt = socketController.getClient(rp.getClientID());
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        long objectID = Long.parseLong(rp.getBody());

        String query = "select count(*) from \"Relation\" where"
                + " \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"relationType\" = " + RelationType.BLOCK.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        boolean isBlocked = false;
        try {
            rs.next();
            if(rs.getInt(1)!=0)
                isBlocked = true;
            if(isBlocked){
                clt.addResponse(
                        new Packet(PacketType.FOLLOW_USER_RES,
                                "error,you can not follow this user",
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
                return;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        /***************************************************************************************/
        query = "select u.\"accountPrivate\" from \"User\" u where"
                + " u.\"userID\" = " + objectID;
        rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        String body = "";
        try {
            rs.next();
            if(rs.getBoolean("accountPrivate")){
                body = "success,your follow request has been sent";
                NotificationController.insertNotification(subjectID,objectID, NotificationType.FOLLOW_REQUEST);
            }
            else{ //Public
                if(insertRelation(subjectID,objectID,RelationType.FOLLOW)) {
                    body = "success,you followed this user";
                    NotificationController.insertNotification(subjectID, objectID, NotificationType.FOLLOWED);
                }
                else {
                    body = "error,you have already followed this user";
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        clt.addResponse(
                new Packet(PacketType.FOLLOW_USER_RES,
                        body,
                        clt.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }

    public void handleUnfollowUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        ClientHandler clt = socketController.getClient(rp.getClientID());
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        long objectID = Long.parseLong(rp.getBody());

        String body = "";
        if(deleteRelation(subjectID,objectID,RelationType.FOLLOW)){
            body = "success,you unfollowed this user";
            NotificationController.insertNotification(subjectID,objectID,NotificationType.UNFOLLOWED);
        }
        else{
            body = "error,you can not unfollow this user";
        }
        clt.addResponse(
                new Packet(PacketType.UNFOLLOW_USER_RES,
                        body,
                        clt.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }

    public static void sendRelationList(ResultSet rs, Packet request, PacketType packetType){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(request.getClientID()).getUserID();

        try {
            ClientHandler clt = socketController.getClient(request.getClientID());
            String body = "";
            while(rs.next()){
                String userImageStr = "";
                byte[] userImage = rs.getBytes("userImage");
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);

                body = rs.getString("userName") + ","
                        + rs.getString("firstName") + ","
                        + rs.getString("lastName") + ","
                        + userImageStr;
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

    public static boolean insertRelation(long subjectID, long objectID, RelationType relationType){
        String query = "select count(*) from \"Relation\" where"
                + " \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"relationType\" = " + relationType.ordinal();
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
        query = "insert into \"Relation\" ("
                + "\"subjectID\","
                + "\"objectID\","
                + "\"relationType\")"
                + " values ("
                + subjectID + ","
                + objectID + ","
                + relationType.ordinal() + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("subjectID:" + subjectID + " " + relationType + " objectID:" + objectID);
            return true;
        }
        else{
            LogHandler.logger.error("could not save Relation in DB");
            return false;
        }
    }

    public static boolean deleteRelation(long subjectID, long objectID, RelationType relationType){
        String query = "delete from \"Relation\" where"
                + " \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"relationType\" = " + relationType.ordinal();
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("subjectID:" + subjectID + " " + relationType + " objectID:" + objectID + " deleted");
            return true;
        }
        else{
            LogHandler.logger.error("could not delete Relation from DB");
            return false;
        }
    }
}
