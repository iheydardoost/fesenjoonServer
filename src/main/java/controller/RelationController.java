package controller;

import main.Main;
import model.ActionType;
import model.Packet;
import model.PacketType;
import model.RelationType;

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
        String body = "";
        if(insertRelation(
                socketController.getClient(rp.getClientID()).getUserID(),
                Long.parseLong(rp.getBody()),
                RelationType.BLOCK)) {
            deleteRelation(
                    socketController.getClient(rp.getClientID()).getUserID(),
                    Long.parseLong(rp.getBody()),
                    RelationType.FOLLOW);
            body = "success";
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
