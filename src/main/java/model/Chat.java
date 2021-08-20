package model;

public class Chat {
    private long chatID;
    private String chatName;
    private ChatType chatType;

    public Chat(long chatID, String chatName, ChatType chatType) {
        this.chatID = chatID;
        this.chatName = chatName;
        this.chatType = chatType;
    }

    public long getChatID() {
        return chatID;
    }

    public void setChatID(long chatID) {
        this.chatID = chatID;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }
}
