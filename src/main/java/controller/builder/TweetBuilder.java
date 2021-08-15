package controller.builder;

import main.Main;
import model.Tweet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TweetBuilder {
    private long parentTweetID;
    private String tweetText;
    private LocalDateTime tweetDateTime;
    private long userID;
    private boolean retweeted;
    private byte[] tweetImage;

    public TweetBuilder() {
        this.parentTweetID = 0;
        this.tweetText = null;
        this.tweetDateTime = null;
        this.userID = 0;
        this.retweeted = false;
        this.tweetImage = null;
    }

    private long findLastTweetID(){
        String query = "select max(\"tweetID\") from \"Tweet\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            long lastTweetID = rs.getLong(1);
            rs.close();
            return lastTweetID;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    private long generateNewTweetID(){
        long lastTweetID = findLastTweetID();
        return (lastTweetID+1);
    }

    public void setParentTweetID(long parentTweetID) {
        this.parentTweetID = parentTweetID;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public void setTweetDateTime(LocalDateTime tweetDateTime) {
        this.tweetDateTime = tweetDateTime;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public void setTweetImage(byte[] tweetImage) {
        this.tweetImage = tweetImage;
    }

    public Tweet build(){
        Tweet tweet = new Tweet(
                generateNewTweetID(), this.parentTweetID, this.tweetText,
                this.tweetDateTime, this.userID,
                this.retweeted, 0, this.tweetImage);

        this.parentTweetID = 0;
        this.tweetText = null;
        this.tweetDateTime = null;
        this.userID = 0;
        this.retweeted = false;
        this.tweetImage = null;

        return tweet;
    }
}
