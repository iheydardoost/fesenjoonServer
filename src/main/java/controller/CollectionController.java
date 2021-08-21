package controller;

import controller.builder.UserCollectionBuilder;
import main.Main;
import model.Packet;
import model.PacketType;
import model.RelationType;
import model.UserCollection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

public class CollectionController {
    private static final UserCollectionBuilder userCollectionBuilder = new UserCollectionBuilder();

    public CollectionController() {
    }

    public void handleGetFolderListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String query = "select * from \"Collection\""
                + " where \"ownerID\" = " + userID
                + " and \"collectionName\" != 'all'";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            while(rs.next()){
                body = rs.getLong("collectionID") + ","
                        + PacketHandler.makeEncodedArg(rs.getString("collectionName"));
                clt.addResponse(
                        new Packet(PacketType.GET_FOLDER_LIST_RES,
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

    public void handleNewFolderReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String body = "error";
        String collectionName = PacketHandler.getDecodedArg(rp.getBody());
        if(insertCollection(userID,collectionName)!=0)
            body = "success";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.NEW_FOLDER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleDeleteFolderReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String body = "error";
        if(deleteCollection(Long.parseLong(rp.getBody())))
            body = "success";
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.DELETE_FOLDER_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetEditFolderListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        long collectionID = Long.parseLong(rp.getBody());

        String query = "select u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userID\""
                + " from \"User\" u"
                + " where u.\"accountActive\" = true"
                + " and exists (select * from \"Relation\" r"
                + " where r.\"relationType\" = " + RelationType.FOLLOW.ordinal()
                + " and (( r.\"subjectID\" = " + userID
                + " and r.\"objectID\" = u.\"userID\")"
                + " or ( r.\"subjectID\" = u.\"userID\""
                + " and r.\"objectID\" = " + userID + ")))";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            boolean isSelected = false;
            while(rs.next()){
                isSelected = isMemberInCollection(rs.getLong("userID"),collectionID);
                body =  PacketHandler.makeEncodedArg(rs.getString("firstName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("lastName")) + ","
                        + PacketHandler.makeEncodedArg(rs.getString("userName")) + ","
                        + isSelected;
                clt.addResponse(
                        new Packet(PacketType.GET_EDIT_FOLDER_LIST_RES,
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

    public void handleSetEditFolderListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        String[] args = rp.getBody().split(",",-1);
        long collectionID = Long.parseLong(args[0]);

        String query = "delete from \"CollectionMember\" where"
                + " \"collectionID\" = " + collectionID;
        Main.getMainController().getDbCommunicator().executeUpdate(query);
        /*********************************************************************/
        int updatedRowsNum = 0;
        long memberID = 0;
        for (int i = 1; i < args.length; i++) {
            memberID = SettingController.findUserID(PacketHandler.getDecodedArg(args[i]));
            if(memberID!=0)
                if(insertCollectionMember(collectionID,memberID))
                    updatedRowsNum++;
        }

        String body = "";
        if(updatedRowsNum==args.length-1){
            body = "success";
            LogHandler.logger.info("collectionID:" + collectionID + " members added");
        }
        else{
            body = "error";
            LogHandler.logger.error("could not add collection member in DB");
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.SET_EDIT_FOLDER_LIST_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public static boolean isMemberInCollection(long memberID,long collectionID){
        String query = "select count(*) from \"CollectionMember\""
                + " where \"collectionID\" = " + collectionID
                + " and \"memberID\" = " + memberID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            if(rs.getInt(1)!=0)
                return true;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public static long insertCollection(long ownerID, String name){
        UserCollection userCollection =
                userCollectionBuilder
                        .setCollectionName(name)
                        .setOwnerID(ownerID)
                        .build();
        String query = "insert into \"Collection\" ("
                + "\"ownerID\","
                + "\"collectionID\","
                + "\"collectionName\")"
                + " select "
                + userCollection.getOwnerID() + ","
                + userCollection.getCollectionID() + ","
                + "'" + userCollection.getCollectionName() + "'"
                + " where not exists (select * from \"Collection\" c"
                + " where c.\"collectionName\" = '" + userCollection.getCollectionName() + "'"
                + " and c.\"ownerID\" = " + userCollection.getOwnerID() + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("collectionID:" + userCollection.getCollectionID()
                                    + " collectionName:" + userCollection.getCollectionName()
                                    + " created");
            return userCollection.getCollectionID();
        }
        else{
            LogHandler.logger.error("could not save collection in DB");
            return 0;
        }
    }

    public static boolean deleteCollection(long collectionID){
        String query = "delete from \"Collection\" where"
                + " \"collectionID\" = " + collectionID;
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        if(updatedRowsNum==1){
            LogHandler.logger.info("collectionID:" + collectionID + " deleted");
            return true;
        }
        else{
            LogHandler.logger.error("could not delete Collection from DB");
            return false;
        }
    }

    public static boolean insertCollectionMember(long collectionID, long memberID){
        String query = "insert into \"CollectionMember\" ("
                + "\"memberID\","
                + "\"collectionID\")"
                + " select "
                + memberID + ","
                + collectionID
                + " where exists (select * from \"Collection\""
                + " where \"collectionID\" = " + collectionID + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==1){
            LogHandler.logger.info("collectionID:" + collectionID
                    + " memberID:" + memberID
                    + " added");
            return true;
        }
        else{
            LogHandler.logger.error("could not add collection member in DB");
            return false;
        }
    }

    public static boolean removeCollectionMember(long collectionID, long memberID){
        String query = "delete from \"CollectionMember\" where"
                + " \"collectionID\" = " + collectionID
                + " and \"memberID\" = " + memberID;
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        if(updatedRowsNum==1){
            LogHandler.logger.info("collectionID:" + collectionID
                                    + " memberID:" + memberID
                                    + " removed");
            return true;
        }
        else{
            LogHandler.logger.error("could not remove Collection member from DB");
            return false;
        }
    }
}
