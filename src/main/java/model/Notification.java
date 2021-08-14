package model;

import java.time.LocalDateTime;

public class Notification extends Message{
    private boolean seen;
    public NOTIFICATION_CONTEX notifContex;
    public enum NOTIFICATION_CONTEX{INFO,MY_REQUEST,OTHERS_REQUEST}

    public Notification() {
    }

//    public Notification(String msgText, LocalDateTime msgDateTime, long userID, NOTIFICATION_CONTEX notifContex) {
//        super(msgText, msgDateTime, userID);
//        this.notifContex = notifContex;
//        this.seen = false;
//    }

}
