package model;

import java.time.LocalDateTime;

public class Tweet{
    private long tweetID;
    private long parentTweetID;
    private String tweetText;
    private LocalDateTime tweetDateTime;
    private long userID;
    private boolean retweeted;
    private int reportedNumber;
    private byte[] tweetImage;


    public Tweet() {
    }

    public Tweet(long tweetID, long parentTweetID, String tweetText,
                 LocalDateTime tweetDateTime, long userID,
                 boolean retweeted, int reportedNumber, byte[] tweetImage) {
        this.tweetID = tweetID;
        this.parentTweetID = parentTweetID;
        this.tweetText = tweetText;
        this.tweetDateTime = tweetDateTime;
        this.userID = userID;
        this.retweeted = retweeted;
        this.reportedNumber = reportedNumber;
        this.tweetImage = tweetImage;
    }

    public long getTweetID() {
        return tweetID;
    }

    public void setTweetID(long tweetID) {
        this.tweetID = tweetID;
    }

    public long getParentTweetID() {
        return parentTweetID;
    }

    public void setParentTweetID(long parentTweetID) {
        this.parentTweetID = parentTweetID;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public LocalDateTime getTweetDateTime() {
        return tweetDateTime;
    }

    public void setTweetDateTime(LocalDateTime tweetDateTime) {
        this.tweetDateTime = tweetDateTime;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public int getReportedNumber() {
        return reportedNumber;
    }

    public void setReportedNumber(int reportedNumber) {
        this.reportedNumber = reportedNumber;
    }

    public byte[] getTweetImage() {
        return tweetImage;
    }

    public void setTweetImage(byte[] tweetImage) {
        this.tweetImage = tweetImage;
    }
}
