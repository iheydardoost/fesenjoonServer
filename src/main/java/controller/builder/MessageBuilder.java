package controller.builder;

import main.Main;
import model.HyperLinkActionType;
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
    private long hyperLinkID;
    private HyperLinkActionType hyperLinkActionType;

    public MessageBuilder() {
        this.msgText = null;
        this.msgDateTime = null;
        this.userID = 0;
        this.chatID = 0;
        this.forwarded = false;
        this.msgStatus = null;
        this.msgImage = null;
        this.hyperLinkID = 0;
        this.hyperLinkActionType = null;
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

    public MessageBuilder setMsgText(String msgText) {
        this.msgText = msgText;
        return this;
    }

    public MessageBuilder setMsgDateTime(LocalDateTime msgDateTime) {
        this.msgDateTime = msgDateTime;
        return this;
    }

    public MessageBuilder setUserID(long userID) {
        this.userID = userID;
        return this;
    }

    public MessageBuilder setChatID(long chatID) {
        this.chatID = chatID;
        return this;
    }

    public MessageBuilder setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
        return this;
    }

    public MessageBuilder setMsgStatus(MessageStatus msgStatus) {
        this.msgStatus = msgStatus;
        return this;
    }

    public MessageBuilder setMsgImage(byte[] msgImage) {
        this.msgImage = msgImage;
        return this;
    }

    public MessageBuilder setHyperLinkActionType(HyperLinkActionType hyperLinkActionType) {
        this.hyperLinkActionType = hyperLinkActionType;
        return this;
    }

    public void setHyperLinkID(long hyperLinkID) {
        this.hyperLinkID = hyperLinkID;
    }

    public Message build(){
        Message message = new Message(
                this.msgText, this.msgDateTime,
                this.userID, this.chatID, generateNewMessageID(),
                this.forwarded, this.msgStatus, this.msgImage,
                this.hyperLinkID, this.hyperLinkActionType);

        this.msgText = null;
        this.msgDateTime = null;
        this.userID = 0;
        this.chatID = 0;
        this.forwarded = false;
        this.msgStatus = null;
        this.msgImage = null;
        this.hyperLinkID = 0;
        this.hyperLinkActionType = null;

        return message;
    }
}
