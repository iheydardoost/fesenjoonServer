package model;

import java.time.LocalDateTime;

public class Message {
    private String msgText;
    private LocalDateTime msgDateTime;
    private long userID;
    private long chatID;
    private long msgID;
    private boolean forwarded;
    private MessageStatus msgStatus;
    private byte[] msgImage;

    public Message() {
    }

    public Message(String msgText, LocalDateTime msgDateTime,
                   long userID, long chatID, long msgID,
                   boolean forwarded, MessageStatus msgStatus, byte[] msgImage) {
        this.msgText = msgText;
        this.msgDateTime = msgDateTime;
        this.userID = userID;
        this.chatID = chatID;
        this.msgID = msgID;
        this.forwarded = forwarded;
        this.msgStatus = msgStatus;
        this.msgImage = msgImage;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public LocalDateTime getMsgDateTime() {
        return msgDateTime;
    }

    public void setMsgDateTime(LocalDateTime msgDateTime) {
        this.msgDateTime = msgDateTime;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getChatID() {
        return chatID;
    }

    public void setChatID(long chatID) {
        this.chatID = chatID;
    }

    public long getMsgID() {
        return msgID;
    }

    public void setMsgID(long msgID) {
        this.msgID = msgID;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public MessageStatus getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(MessageStatus msgStatus) {
        this.msgStatus = msgStatus;
    }

    public byte[] getMsgImage() {
        return msgImage;
    }

    public void setMsgImage(byte[] msgImage) {
        this.msgImage = msgImage;
    }
}
