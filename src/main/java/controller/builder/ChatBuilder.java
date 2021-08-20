package controller.builder;

import main.Main;
import model.Chat;
import model.ChatType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatBuilder {
    private String chatName;
    private ChatType chatType;

    public ChatBuilder() {
        this.chatName = null;
        this.chatType = null;
    }

    private long findLastChatID(){
        String query = "select max(\"chatID\") from \"Chat\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            long lastChatID = rs.getLong(1);
            rs.close();
            return lastChatID;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    private long generateNewChatID(){
        long lastChatID = findLastChatID();
        return (lastChatID+1);
    }

    public ChatBuilder setChatName(String chatName) {
        this.chatName = chatName;
        return this;
    }

    public ChatBuilder setChatType(ChatType chatType) {
        this.chatType = chatType;
        return this;
    }

    public Chat build(){
        Chat chat = new Chat(generateNewChatID(),this.chatName,this.chatType);
        this.chatName = null;
        this.chatType = null;
        return chat;
    }
}
