package controller;

import model.Packet;
import model.PacketType;

public class PacketHandler {

    public PacketHandler() {
    }

    public String makePacketStr(Packet packet){
        String str = packet.getRequestID() + ",";
        if(packet.isAuthTokenAvailable())
            str += (packet.getAuthToken() + ",");
        else
            str += "null,";
        str += (packet.getPacketType().toString()+","+packet.getBody()+",$");
        return str;
    }

    public Packet parsePacket(String str, int clientID){
        String[] args = str.split(",");
        if(args.length<3) return null;
        /**************************************************/
        int myRequestID = Integer.parseInt(args[0]);
        boolean myAuthTokenAvailable = true;
        int myAuthToken = 0;
        try {
            myAuthToken = Integer.parseInt(args[1]);
        }catch (NumberFormatException ne){
            myAuthTokenAvailable = false;
        }
        /**************************************************/
        PacketType myPacketType = parsePacketType(args[2]);
        /**************************************************/
        String myBody = "";
        for(int i=3;i<args.length;i++){
            if(!args[i].equals("$")) {
                if(i!=3) myBody += ",";
                myBody += args[i];
            }
        }
        return new Packet(myPacketType,myBody,myAuthToken,myAuthTokenAvailable,clientID,myRequestID);
    }

    private PacketType parsePacketType(String str){
        if(str.equals(PacketType.SIGN_UP_REQ.toString()))
            return PacketType.SIGN_UP_REQ;
        else if(str.equals(PacketType.SIGN_IP_RES.toString()))
            return PacketType.SIGN_IP_RES;
        else if(str.equals(PacketType.LOG_IN_REQ.toString()))
            return PacketType.LOG_IN_REQ;
        else if(str.equals(PacketType.LOG_IN_RES.toString()))
            return PacketType.LOG_IN_RES;
        else
            return null;
    }
}
