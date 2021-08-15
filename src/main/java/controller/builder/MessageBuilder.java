package controller.builder;

import main.Main;
import model.Message;
import model.MessageStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MessageBuilder {
    private String msgText;
    private LocalDateTime msgDateTime;
    private long userID;
    private long chatID;
    private boolean forwarded;
    private MessageStatus msgStatus;
    private byte[] msgImage;

    public MessageBuilder() {
        this.msgText = null;
        this.msgDateTime = null;
        this.userID = 0;
        this.chatID = 0;
        this.forwarded = false;
        this.msgStatus = null;
        this.msgImage = null;
    }

    private long findLastMessageID(){
        String query = "select max(\"msgID\") from \"message\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            long lastMessageID = rs.getLong(1);
            rs.close();
            return lastMessageID;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    private long generateNewMessageID(){
        long lastMessageID = findLastMessageID();
        return (lastMessageID+1);
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void setMsgDateTime(LocalDateTime msgDateTime) {
        this.msgDateTime = msgDateTime;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setChatID(long chatID) {
        this.chatID = chatID;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public void setMsgStatus(MessageStatus msgStatus) {
        this.msgStatus = msgStatus;
    }

    public void setMsgImage(byte[] msgImage) {
        this.msgImage = msgImage;
    }

    public Message build(){
        Message message = new Message(
                this.msgText, this.msgDateTime,
                this.userID, this.chatID, generateNewMessageID(),
                this.forwarded, this.msgStatus, this.msgImage);

        this.msgText = null;
        this.msgDateTime = null;
        this.userID = 0;
        this.chatID = 0;
        this.forwarded = false;
        this.msgStatus = null;
        this.msgImage = null;

        return message;
    }
}
