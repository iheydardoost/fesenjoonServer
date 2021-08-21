package controller;

import main.Main;
import model.LastSeenStatus;
import model.Packet;
import model.PacketType;
import model.RelationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

public class PrivateController {

    public void handleEditUserInfoReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] bodyArgs = rp.getBody().split(",",-1);
        String variable = bodyArgs[0];
        String value;
        if(variable.equals("bio")){
            String bio = PacketHandler.getDecodedArg(bodyArgs[1]);
            for(int i=2; i<bodyArgs.length; i++){
                bio += ("," + bodyArgs[i]);
            }
            value = bio;
        }
        else if(variable.equals("userImage")){
            value = bodyArgs[1];
        }
        else {
            value = PacketHandler.getDecodedArg(bodyArgs[1]);
        }
        /************************************************************/
        int updatedRowsNum = 0;
        String query = "";
        if(variable.equals("userImage")){
            byte[] userImage = Base64.getDecoder().decode(value);
            query = "update \"User\" set \"userImage\" = ? where \"userID\" = " + userID;
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdateBytea(query, userImage);
        }
        else{
            query = "update \"User\" set \"" + variable + "\" = "
                    + "'" + value + "'"
                    + " where \"userID\" = " + userID;
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        }
        /************************************************************/
        String body = "";
        if(updatedRowsNum==1){
            body = "success," + variable;
            if(variable.equals("userImage"))
                LogHandler.logger.info("userID: " + userID + " changed userImage");
            else
                LogHandler.logger.info(variable + " changed to " + value + " (userID: " + userID + ")");
        }
        else if(updatedRowsNum==0){
            body = "error," + variable;
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.EDIT_USER_INFO_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetPrivateInfo(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select * from \"User\" u"
                + " where u.\"userID\" = " + userID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /************************************************************/
        ClientHandler clt = socketController.getClient(rp.getClientID());
        try {
            if(rs.next()) {
                String userImageStr = "";
                byte[] userImage = rs.getBytes("userImage");
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);
                String dateOfBirth = "";
                if(rs.getDate("dateOfBirth")!=null)
                    dateOfBirth = rs.getDate("dateOfBirth").toString();
                String body = PacketHandler.makeEncodedArg(rs.getString("userName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("lastName")) + ","
                        + dateOfBirth + ","
                        + PacketHandler.makeEncodedArg(rs.getString("email")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("phoneNumber")) + ","
                        + userImageStr + ","
                        + PacketHandler.makeEncodedArg(rs.getString("bio"));
                clt.addResponse(
                        new Packet(PacketType.GET_PRIVATE_INFO_RES,
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

    public void handleGetEditInfo(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select * from \"User\" u"
                + " where u.\"userID\" = " + userID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /************************************************************/
        ClientHandler clt = socketController.getClient(rp.getClientID());
        try {
            if(rs.next()) {
                String userImageStr = "";
                byte[] userImage = rs.getBytes("userImage");
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);
                String dateOfBirth = "";
                if(rs.getDate("dateOfBirth")!=null)
                    dateOfBirth = rs.getDate("dateOfBirth").toString();
                String body = PacketHandler.makeEncodedArg(rs.getString("userName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("lastName")) + ","
                        + dateOfBirth + ","
                        + PacketHandler.makeEncodedArg(rs.getString("email")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("phoneNumber")) + ","
                        + userImageStr + ","
                        + PacketHandler.makeEncodedArg(rs.getString("bio"));
                clt.addResponse(
                        new Packet(PacketType.GET_EDIT_INFO_RES,
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

    public void handleGetUserInfo(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        long objectID = Long.parseLong(rp.getBody());
        ClientHandler clt = socketController.getClient(rp.getClientID());

        String query = "select * from \"User\" u"
                + " where u.\"userID\" = " + objectID
                + " and u.\"accountActive\" = true";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);

        query = "select count(*) from \"Relation\" r"
                + " where r.\"subjectID\" = " + subjectID
                + " and r.\"objectID\" = " + objectID
                + " and r.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        ResultSet rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
        /************************************************************/
        boolean isFollowing = false;
        String lastSeenStr = "";
        String dateOfBirthStr="",email="",phoneNumber="";
        try {
            rs1.next();
            if(rs1.getInt(1)!=0)
                isFollowing = true;

            if(rs.next()) {
                if (!Main.getMainController().getSocketController().isUserOnline(objectID)) {
                    LastSeenStatus lastSeenStatus = LastSeenStatus.values()[rs.getInt("lastSeenStatus")];
                    LocalDate lastSeen = rs.getDate("lastSeen").toLocalDate();
                    switch (lastSeenStatus) {
                        case EVERYONE:
                            lastSeenStr = lastSeen.toString();
                            break;
                        case FOLLOWINGS:
                            if (isFollowing)
                                lastSeenStr = lastSeen.toString();
                            else
                                lastSeenStr = "last seen recently";
                            break;
                        case NO_ONE:
                            lastSeenStr = "last seen recently";
                            break;
                    }
                } else {
                    lastSeenStr = "online";
                }

                if(isFollowing){
                    if(rs.getDate("dateOfBirth")!=null)
                        dateOfBirthStr = rs.getDate("dateOfBirth").toString();
                    email = rs.getString("email");
                    phoneNumber = rs.getString("phoneNumber");
                }
            }
            else {
                clt.addResponse(
                        new Packet(PacketType.GET_USER_INFO_RES,
                                "error",
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
        /************************************************************/
        try {
            String userImageStr = "";
            byte[] userImage = rs.getBytes("userImage");
            if(userImage!=null)
                userImageStr = Base64.getEncoder().encodeToString(userImage);
            String body = "success,"
                    + PacketHandler.makeEncodedArg(rs.getString("userName")) + ","
                    + PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                    + PacketHandler.makeEncodedArg(rs.getString("lastName")) + ","
                    + dateOfBirthStr + ","
                    + PacketHandler.makeEncodedArg(email) + ","
                    + PacketHandler.makeEncodedArg(phoneNumber) + ","
                    + userImageStr + ","
                    + lastSeenStr + ","
                    + isFollowing + ","
                    + PacketHandler.makeEncodedArg(rs.getString("bio"));
            clt.addResponse(
                    new Packet(PacketType.GET_USER_INFO_RES,
                            body,
                            clt.getAuthToken(),
                            true,
                            rp.getClientID(),
                            rp.getRequestID())
            );
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }
}
