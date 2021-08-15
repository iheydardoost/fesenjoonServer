package model;

public class Notification extends Message{
    private boolean seen;
    public NOTIFICATION_CONTEX notifContex;
    public enum NOTIFICATION_CONTEX{INFO,MY_REQUEST,OTHERS_REQUEST}

    public Notification() {
    }

}
