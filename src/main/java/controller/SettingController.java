package controller;

import main.Main;
import model.LastSeenStatus;
import model.Packet;
import model.PacketType;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        String[] bodyArgs = rp.getBody().split(",");
        String variable = bodyArgs[0];
        String value = bodyArgs[1];

        String query = "";
        int updatedRowsNum = 0;
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        if(variable.equals("accountPrivate")) {
            query = "update \"User\" set \"accountPrivate\" = "
                    + value
                    + " where \"userID\" = "
                    + userID;
        }
        else if(variable.equals("accountActive")){
            query = "update \"User\" set \"accountActive\" = "
                    + value
                    + " where \"userID\" = "
                    + userID;
        }
        else if(variable.equals("lastSeenStatus")){
            query = "update \"User\" set \"lastSeenStatus\" = "
                    + LastSeenStatus.valueOf(value).ordinal()
                    + " where \"userID\" = "
                    + userID;
        }
        else if(variable.equals("password")){
            query = "update \"User\" set \"passwordHash\" = "
                    + value.hashCode()
                    + " where \"userID\" = "
                    + userID;
        }
        updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.CHANGE_SETTING_RES,
                                    "success," + variable,
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.info(variable + " changed to " + value + " (userID: " + userID + ")");
        }
        else if(updatedRowsNum==0){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.CHANGE_SETTING_RES,
                                    "error," + variable,
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
        }
    }

    public void handleDeleteUserReq(Packet rp){
        String query = "";
        int deletedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        if(deletedRowsNum==1){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.DELETE_USER_RES,
                                    "success",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            socketController.getClient(rp.getClientID())
                    .setAuthToken(-1)
                    .setUserID(0);
            LogHandler.logger.info("user (userID: " + userID + ") deleted");
        }
        else if(deletedRowsNum==0){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.DELETE_USER_RES,
                                    "error",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
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
                                "success,logged out successfully",
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
}
