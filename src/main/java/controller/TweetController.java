package controller;

import controller.builder.TweetBuilder;
import main.Main;
import model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

public class TweetController {
    private TweetBuilder tweetBuilder;

    public TweetController() {
        this.tweetBuilder = new TweetBuilder();
    }

    public void handleNewTweetReq(Packet rp){
        String[] args = rp.getBody().split(",",-1);
        String tweetText = args[0];
        LocalDateTime tweetDateTime = LocalDateTime.parse(args[1]);
        long userID = Long.parseLong(args[2]);
        long parentTweetID = 0;
        if(!args[3].isEmpty())
            parentTweetID = Long.parseLong(args[3]);
        boolean retweeted = Boolean.parseBoolean(args[4]);
        byte[] tweetImage = null;
        if(!args[5].isEmpty())
            tweetImage = Base64.getDecoder().decode(args[5]);
        /***********************************************************/
        Tweet tweet =
                tweetBuilder.setTweetText(tweetText)
                        .setTweetDateTime(tweetDateTime)
                        .setUserID(userID)
                        .setParentTweetID(parentTweetID)
                        .setRetweeted(retweeted)
                        .setTweetImage(tweetImage)
                        .build();

        String query = "insert into \"Tweet\""
                + " ("
                + "\"tweetText\"" + ","
                + "\"tweetDateTime\"" + ","
                + "\"userID\"" + ","
                + "\"tweetID\"" + ","
                + "\"parentTweetID\"" + ","
                + "\"retweeted\"" + ","
                + "\"reportedNumber\""
                + ") "
                + "values"
                + " ("
                + "'" + tweet.getTweetText() + "'" + ","
                + "'" + tweet.getTweetDateTime().toLocalDate().toString() + "T"
                + tweet.getTweetDateTime().toLocalTime().toString() + "'" + ","
                + tweet.getUserID() + ","
                + tweet.getTweetID() + ","
                + tweet.getParentTweetID() + ","
                + tweet.isRetweeted() + ","
                + tweet.getReportedNumber()
                + ")";
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(tweetImage!=null) {
            query = "update \"Tweet\" set \"tweetImage\" = ? where \"tweetID\" = " + tweet.getTweetID();
            int updatedImageNum = Main.getMainController().getDbCommunicator().executeUpdateBytea(query, tweetImage);
        }
        /***********************************************************/
        SocketController socketController = Main.getMainController().getSocketController();
        if(updatedRowsNum==1){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.NEW_TWEET_RES,
                                    "success," + tweet.isRetweeted(),
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.info("Tweet with tweetID:" + tweet.getTweetID() + " created successfully");
        }
        else{
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.NEW_TWEET_RES,
                                    "error," + tweet.isRetweeted(),
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.error("could not create new Tweet in DB");
        }
    }

    public void handleReportTweetReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        long tweetID = Long.parseLong(rp.getBody());

        String query = "select count(*) from \"Like/Spam\" where"
                + " \"tweetID\" = " + tweetID
                + " and \"userID\" = " + userID
                + " and \"actionType\" = " + ActionType.SPAM.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        int rowsExisted=0;
        try {
            rs.next();
            rowsExisted = rs.getInt(1);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        if(rowsExisted!=0){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.REPORT_TWEET_RES,
                                    "error",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            return;
        }
        /************************************************************************/
        query = "update \"Tweet\" set \"reportedNumber\" = \"reportedNumber\" + 1"
                + " where \"tweetID\" = " + tweetID;
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);

        query = "insert into \"Like/Spam\" ("
                + "\"tweetID\","
                + "\"userID\","
                + "\"actionType\")"
                + " values ("
                + tweetID + ","
                + userID + ","
                + ActionType.SPAM.ordinal() + ")";
        updatedRowsNum += Main.getMainController().getDbCommunicator().executeUpdate(query);

        if(updatedRowsNum==2){
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.REPORT_TWEET_RES,
                                    "success",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.info("Tweet with tweetID:" + tweetID + " reported by userID:" + userID);
        }
        else{
            socketController.getClient(rp.getClientID())
                    .addResponse(
                            new Packet(PacketType.REPORT_TWEET_RES,
                                    "error",
                                    rp.getAuthToken(),
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.error("could not report Tweet in DB");
        }
    }

    public void handleLikeTweetReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        long tweetID = Long.parseLong(rp.getBody());

        String query = "select count(*) from \"Like/Spam\" where"
                + " \"tweetID\" = " + tweetID
                + " and \"userID\" = " + userID
                + " and \"actionType\" = " + ActionType.LIKE.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        int rowsExisted=0;
        try {
            rs.next();
            rowsExisted = rs.getInt(1);
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        /************************************************************************/
        int updatedRowsNum=0;
        if(rowsExisted==0){
            query = "insert into \"Like/Spam\" ("
                    + "\"tweetID\","
                    + "\"userID\","
                    + "\"actionType\")"
                    + " values ("
                    + tweetID + ","
                    + userID + ","
                    + ActionType.LIKE.ordinal() + ")";
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
            if(updatedRowsNum==1){
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LIKE_TWEET_RES,
                                        "success,true",
                                        rp.getAuthToken(),
                                        true,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                LogHandler.logger.info("Tweet with tweetID:" + tweetID + " liked by userID:" + userID);
            }
            else{
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LIKE_TWEET_RES,
                                        "error,false",
                                        rp.getAuthToken(),
                                        true,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                LogHandler.logger.error("could not like Tweet in DB");
            }
        }
        else{
            query = "delete from \"Like/Spam\""
                    + " where \"tweetID\" = " + tweetID
                    + " and \"userID\" = " + userID
                    + " and \"actionType\" = " + ActionType.LIKE.ordinal();
            updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
            if(updatedRowsNum==1){
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LIKE_TWEET_RES,
                                        "success,false",
                                        rp.getAuthToken(),
                                        true,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                LogHandler.logger.info("Tweet with tweetID:" + tweetID + " disliked by userID:" + userID);
            }
            else{
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LIKE_TWEET_RES,
                                        "error,true",
                                        rp.getAuthToken(),
                                        true,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                LogHandler.logger.error("could not dislike Tweet in DB");
            }
        }
    }
}
