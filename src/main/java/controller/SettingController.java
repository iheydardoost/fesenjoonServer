package controller;

import main.Main;
import model.Packet;

public class SettingController {

    public SettingController() {
    }

    public void handleChangeSetting(Packet rp){
        String[] bodyArgs = rp.getBody().split(",");
        String variable = bodyArgs[0];
        String value = bodyArgs[1];

        String query = "";
        int updatedRowsNum = 0;
        SocketController socketController = Main.getMainController().getSocketController();
        if(variable.equals("accountPrivate")) {
            query = "update \"User\" set \"accountPrivate\" = "
                    + value
                    + " where \"userID\" = "
                    + socketController.getClient(rp.getClientID()).getUserID();
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        }
        else if(variable.equals("accountActive")){
            query = "update \"User\" set \"accountActive\" = "
                    + value
                    + " where \"userID\" = "
                    + socketController.getClient(rp.getClientID()).getUserID();
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        }
        else if(variable.equals("lastSeenStatus")){
            query = "update \"User\" set \"lastSeenStatus\" = "
                    + value
                    + " where \"userID\" = "
                    + socketController.getClient(rp.getClientID()).getUserID();
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        }
        else if(variable.equals("password")){

        }
    }

    public void handleDeleteUser(Packet rp){

    }

    public void handleLogout(Packet rp){

    }
}
