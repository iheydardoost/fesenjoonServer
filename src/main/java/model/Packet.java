package model;

public class Packet {
    private int clientID;
    private int requestID;
    private int authToken;
    private boolean authTokenAvailable;
    private PacketType packetType;
    private String body;

    public Packet(PacketType packetType, String body, int authToken, boolean authTokenAvailable, int clientID, int requestID) {
        this.packetType = packetType;
        this.body = body;
        this.authToken = authToken;
        this.authTokenAvailable = authTokenAvailable;
        this.requestID = requestID;
        this.clientID = clientID;

    }

    public int getRequestID() {
        return requestID;
    }

    public int getAuthToken() {
        return authToken;
    }

    public boolean isAuthTokenAvailable() {
        return authTokenAvailable;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public String getBody() {
        return body;
    }

    public int getClientID() {
        return clientID;
    }
}