package controller;

import main.Main;
import model.ActionType;
import model.Packet;
import model.PacketType;
import model.RelationType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationController {

    public void handleReportUserReq(Packet rp){
        handleRelationReq(rp,RelationType.REPORT,PacketType.REPORT_USER_RES);
    }

    public void handleMuteUserReq(Packet rp){
        handleRelationReq(rp,RelationType.MUTE,PacketType.MUTE_USER_RES);
    }

    public void handleBlockUserReq(Packet rp){
        handleRelationReq(rp,RelationType.BLOCK,PacketType.BLOCK_USER_RES);
    }

    private void handleRelationReq(Packet rp, RelationType relationType, PacketType responseType){
        SocketController socketController = Main.getMainController().getSocketController();
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        long objectID = Long.parseLong(rp.getBody());

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
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(responseType,
                                    "error",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            return;
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
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(responseType,
                                    "success",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.info("subjectID:" + subjectID + " " + relationType + " objectID:" + objectID);
        }
        else{
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(responseType,
                                    "error",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.error("could not save Relation in DB");
        }
    }
}
