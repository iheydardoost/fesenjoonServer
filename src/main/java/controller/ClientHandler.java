package controller;

import main.LoopHandler;
import main.Main;
import model.Packet;
import model.PacketType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class ClientHandler implements Runnable{
    private LoopHandler loopHandler;
    private Socket socket;
    private PacketHandler packetHandler;
    private OutputStream output;
    private InputStream input;
    private int authToken;
    private int clientID;
    private long userID;
    private final LinkedList<Packet> requests;
    private final LinkedList<Packet> responses;
    private boolean wantToUpdateChatroom;
    private boolean wantToUpdateChat;
    private long chatIDToUpdate;

    public ClientHandler(Socket socket, int clientID) {
        requests = new LinkedList<>();
        responses = new LinkedList<>();
        this.clientID = clientID;
        this.wantToUpdateChat = false;
        this.wantToUpdateChatroom = false;
        this.chatIDToUpdate = 0;

        this.socket = socket;
        try {
            this.output = socket.getOutputStream();
            this.input = socket.getInputStream();
        } catch (IOException e) {
            //e.printStackTrace();
            LogHandler.logger.error("socket output/input stream has problem");
        }
        packetHandler = new PacketHandler();

        loopHandler = new LoopHandler(200, this);
    }

    @Override
    public void run() {
        String inStr = "";
        char ch;
        try {
            synchronized (requests) {
                if (input.available() > 0) {
                    inStr = "";
                    ch = '0';
                    while(ch != '$'){
                        ch = (char) input.read();
                        inStr += ch;
                    }
                    LogHandler.logger.info("inStrServer: "+inStr);
                    Packet request = packetHandler.parsePacket(inStr,clientID);
                    if(request!=null) {
                        if ((request.isAuthTokenAvailable() && request.getAuthToken() == this.authToken)
                                || request.getPacketType() == PacketType.SIGN_UP_REQ
                                || request.getPacketType() == PacketType.LOG_IN_REQ)
                            requests.add(request);
                    }
                    if(request.getPacketType()==PacketType.BYE) {
                        SettingController.updateLastSeen(request);
                        Main.getMainController().getSocketController().removeClient(this);
                        this.socket.close();
                        this.loopHandler.pause();
                    }
                }
                if(requests.size()>0){
                    Main.getMainController().handleRequest(requests.removeFirst());
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            LogHandler.logger.error("network input stream could not work");
        }

        try {
            synchronized (responses) {
                if (!responses.isEmpty()) {
                    Packet response = responses.removeFirst();
                    output.write(packetHandler.makePacketStr(response).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            LogHandler.logger.error("network output stream could not work");
        }
    }

    public void addResponse(Packet rp){
        synchronized (responses) {
            responses.add(rp);
        }
    }

    public int getAuthToken() {
        return authToken;
    }

    public int getClientID() {
        return clientID;
    }

    public long getUserID() {
        return userID;
    }

    public ClientHandler setUserID(long userID) {
        this.userID = userID;
        return this;
    }

    public ClientHandler setAuthToken(int authToken){
        this.authToken = authToken;
        return this;
    }

    public boolean isWantToUpdateChatroom() {
        return wantToUpdateChatroom;
    }

    public void setWantToUpdateChatroom(boolean wantToUpdateChatroom) {
        this.wantToUpdateChatroom = wantToUpdateChatroom;
    }

    public boolean isWantToUpdateChat() {
        return wantToUpdateChat;
    }

    public void setWantToUpdateChat(boolean wantToUpdateChat) {
        this.wantToUpdateChat = wantToUpdateChat;
    }
}