package model;

public class UserCollection {
    private String collectionName;
    private long ownerID;
    private long collectionID;

    public UserCollection(String collectionName, long ownerID, long collectionID) {
        this.collectionName = collectionName;
        this.ownerID = ownerID;
        this.collectionID = collectionID;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    public long getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(long collectionID) {
        this.collectionID = collectionID;
    }
}
