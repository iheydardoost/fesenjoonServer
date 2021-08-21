package controller;

import model.Packet;
import model.PacketType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PacketHandler {

    public PacketHandler() {
    }

    public static String makeEncodedArg(String arg){
        byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
        String encodedStr = Base64.getEncoder().encodeToString(bytes);
        return encodedStr;
    }

    public static String getDecodedArg(String arg){
        byte[] bytes = Base64.getDecoder().decode(arg);
        String decodedStr = new String(bytes,StandardCharsets.UTF_8);
        return decodedStr;
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
        PacketType myPacketType = PacketType.valueOf(args[2]);
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
}
