package controller.builder;

import main.Main;
import model.UserCollection;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserCollectionBuilder {
    private String collectionName;
    private long ownerID;

    public UserCollectionBuilder() {
        this.collectionName = null;
        this.ownerID = 0;
    }

    private long findLastCollectionID(){
        String query = "select max(\"collectionID\") from \"Collection\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            long lastCollectionID = rs.getLong(1);
            rs.close();
            return lastCollectionID;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    private long generateNewCollectionID(){
        long lastCollectionID = findLastCollectionID();
        return (lastCollectionID+1);
    }

    public UserCollectionBuilder setCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public UserCollectionBuilder setOwnerID(long ownerID) {
        this.ownerID = ownerID;
        return this;
    }

    public UserCollection build(){
        UserCollection userCollection =
                new UserCollection(this.collectionName,this.ownerID,generateNewCollectionID());
        this.collectionName = null;
        this.ownerID = 0;
        return userCollection;
    }
}
