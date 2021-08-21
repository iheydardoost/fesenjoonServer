package controller;

import main.Main;
import model.ChatType;
import model.LastSeenStatus;
import model.Packet;
import model.PacketType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SettingController {

    public SettingController() {
    }

    public void handleSettingInfoReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select * from \"User\" u where u.\"userID\" = " + userID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);

        LastSeenStatus lastSeenStatus;
        boolean accountPrivate;
        boolean accountActive;
        try {
            rs.next();
            lastSeenStatus = LastSeenStatus.values()[rs.getInt("lastSeenStatus")];
            accountPrivate = rs.getBoolean("accountPrivate");
            accountActive = rs.getBoolean("accountActive");
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.SETTING_INFO_RES,
                                    "lastSeenStatus," + lastSeenStatus + ","
                                    + "accountPrivate," + accountPrivate + ","
                                    + "accountActive," + accountActive,
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleChangeSettingReq(Packet rp){
        String[] bodyArgs = rp.getBody().split(",",-1);
        String variable = bodyArgs[0];
        String value = PacketHandler.getDecodedArg(bodyArgs[1]);

        int updatedRowsNum = 0;
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String query = "";
        if(variable.equals("accountPrivate")) {
            query = "update \"User\" set \"accountPrivate\" = "
                    + value;
        }
        else if(variable.equals("accountActive")){
            query = "update \"User\" set \"accountActive\" = "
                    + value;
        }
        else if(variable.equals("lastSeenStatus")){
            query = "update \"User\" set \"lastSeenStatus\" = "
                    + LastSeenStatus.valueOf(value).ordinal();
        }
        else if(variable.equals("password")){
            query = "update \"User\" set \"passwordHash\" = "
                    + value.hashCode();
        }
        query += " where \"userID\" = " + userID;
        updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        String body = "";
        if(updatedRowsNum==1){
            body = "success," + variable;
            LogHandler.logger.info(variable + " changed to " + value + " (userID: " + userID + ")");
        }
        else if(updatedRowsNum==0){
            body = "error," + variable;
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.CHANGE_SETTING_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleDeleteUserReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        deleteMessagingDefaults(userID);
        String query = "delete from \"User\" where \"userID\" = " + userID;
        int deletedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        String body = "";
        if(deletedRowsNum==1){
            body = "success";
            socketController.getClient(rp.getClientID())
                    .setAuthToken(-1)
                    .setUserID(0);
            LogHandler.logger.info("user (userID: " + userID + ") deleted");
        }
        else if(deletedRowsNum==0){
            body = "error";
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.DELETE_USER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    private void deleteMessagingDefaults(long userID){
        String query = "select c.\"chatID\" from \"Chat\" c where"
                + " c.\"chatName\" = 'savedMessages'"
                + " and c.\"chatType\" = " + ChatType.SAVED_MESSAGES.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        long chatID = 0;
        try {
            if(rs.next()){
                chatID = rs.getLong("chatID");
                ChatController.deleteChat(chatID);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    public void handleLogoutReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        socketController.getClient(rp.getClientID())
                .setAuthToken(-1)
                .setUserID(0);

        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.LOG_OUT_RES,
                                "success",
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
        LogHandler.logger.info(
                "userID:"
                + socketController.getClient(rp.getClientID()).getUserID()
                        + " logged out");
    }

    public static void updateLastSeen(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "update \"User\" set \"lastSeen\" = "
                + "'" + LocalDate.now() + "'"
                + " where \"userID\" = " + userID;
        Main.getMainController().getDbCommunicator().executeUpdate(query);
    }

    public static long findUserID(String userName){
        String query = "select \"userID\" from \"User\""
                + " where \"userName\" = '" + userName + "'";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            if(rs.next())
                return rs.getLong("userID");
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }
}
