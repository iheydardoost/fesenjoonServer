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
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",",-1);
        String tweetText = args[0];
        LocalDateTime tweetDateTime = LocalDateTime.parse(args[1]);
        long parentTweetID = 0;
        if(!args[2].isEmpty())
            parentTweetID = Long.parseLong(args[2]);
        boolean retweeted = Boolean.parseBoolean(args[3]);
        byte[] tweetImage = null;
        if(!args[4].isEmpty())
            tweetImage = Base64.getDecoder().decode(args[4]);
        /***********************************************************/
        String query = "select count(*) from \"Tweet\" where"
                + " \"userID\" = " + userID
                + " and \"retweeted\" = true"
                + " and \"tweetText\" = '" + tweetText + "'";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            if(rs.getInt(1)!=0)
                return;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        /***********************************************************/
        Tweet tweet =
                tweetBuilder.setTweetText(tweetText)
                        .setTweetDateTime(tweetDateTime)
                        .setUserID(userID)
                        .setParentTweetID(parentTweetID)
                        .setRetweeted(retweeted)
                        .setTweetImage(tweetImage)
                        .build();

        query = "insert into \"Tweet\""
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
        String body = "";
        if(updatedRowsNum==1){
            body = "success," + tweet.isRetweeted();
            LogHandler.logger.info("Tweet with tweetID:" + tweet.getTweetID() + " created successfully");
        }
        else{
            body = "error," + tweet.isRetweeted();
            LogHandler.logger.error("could not create new Tweet in DB");
        }
        PacketType packetType;
        if(tweet.isRetweeted())
            packetType = PacketType.RETWEET_RES;
        else
            packetType = PacketType.NEW_TWEET_RES;
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(packetType,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
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

        String body = "";
        if(updatedRowsNum==2){
            body = "success";
            LogHandler.logger.info("Tweet with tweetID:" + tweetID + " reported by userID:" + userID);
        }
        else{
            body = "error";
            LogHandler.logger.error("could not report Tweet in DB");
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.REPORT_TWEET_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
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
        String body = "";
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
                body = "success,true";
                LogHandler.logger.info("Tweet with tweetID:" + tweetID + " liked by userID:" + userID);
            }
            else{
                body = "error,false";
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
                body = "success,false";
                LogHandler.logger.info("Tweet with tweetID:" + tweetID + " disliked by userID:" + userID);
            }
            else{
                body = "error,true";
                LogHandler.logger.error("could not dislike Tweet in DB");
            }
        }
        socketController.getClient(rp.getClientID())
                .addResponse(
                        new Packet(PacketType.LIKE_TWEET_RES,
                                body,
                                rp.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
    }

    public void handleGetTweetReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();
        long tweetID = Long.parseLong(rp.getBody());

        String subQuery1 = "select * from \"Relation\" rr"
                + " where rr.\"subjectID\" = " + userID
                + " and rr.\"objectID\" = t.\"userID\""
                + " and rr.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        String subQuery2 = "select * from \"Relation\" rr"
                + " where rr.\"subjectID\" = " + userID
                + " and rr.\"objectID\" = t.\"userID\""
                + " and rr.\"relationType\" = " + RelationType.BLOCK.ordinal();
        String query = "select t.*, u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\", u.\"userID\""
                + " from \"Tweet\" t, \"User\" u"
                + " where t.\"tweetID\" = " + tweetID
                + " and u.\"accountActive\" = true"
                + " and not exists (" + subQuery2 + ")"
                + " and (u.\"accountPrivate\" = false"
                + " or (u.\"accountPrivate\" = true"
                + " and exists (" + subQuery1 + ")))"
                + " and u.\"userID\" = t.\"userID\"";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        try {
            ResultSet rs1;
            int likedNum=0, commentNum=0;
            boolean youLiked = false;
            boolean isMute = false;
            String body = "";
            ClientHandler clt = socketController.getClient(rp.getClientID());
            if(rs.next()){
                likedNum = getLikedNum(rs.getLong("tweetID"));
                commentNum = getCommentNum(rs.getLong("tweetID"));
                youLiked = getYouLiked(userID,rs.getLong("tweetID"));
                isMute = getIsMute(userID,rs.getLong("userID"));
                /************************************/
                String tweetImageStr = "", userImageStr = "";
                byte[] tweetImage = rs.getBytes("tweetImage");
                byte[] userImage = rs.getBytes("userImage");
                if(tweetImage!=null)
                    tweetImageStr = Base64.getEncoder().encodeToString(tweetImage);
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);

                body = rs.getString("tweetText") + ","
                        + rs.getTimestamp("tweetDateTime").toLocalDateTime().toString() + ","
                        + rs.getLong("userID") + ","
                        + rs.getLong("tweetID") + ","
                        + rs.getBoolean("retweeted") + ","
                        + youLiked + ","
                        + tweetImageStr + ","
                        + rs.getString("userName") + ","
                        + rs.getString("firstName") + ","
                        + rs.getString("lastName") + ","
                        + userImageStr + ","
                        + likedNum + ","
                        + commentNum + ","
                        + isMute;
                clt.addResponse(
                        new Packet(PacketType.GET_TWEET_RES,
                                body,
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public void handleGetCommentsReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",", -1);
        int maxNum = Integer.parseInt(args[0]);
        LocalDateTime lastTweetDateTime = null;
        if(!args[1].isEmpty())
            lastTweetDateTime = LocalDateTime.parse(args[1]);
        long parentTweetID = Long.parseLong(args[2]);
        /***************************************************************************************/
        String subQuery1 = "select * from \"Relation\" rr"
                + " where rr.\"subjectID\" = " + userID
                + " and rr.\"objectID\" = t.\"userID\""
                + " and rr.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        String subQuery2 = "select * from \"Relation\" rr"
                + " where rr.\"subjectID\" = " + userID
                + " and rr.\"objectID\" = t.\"userID\""
                + " and rr.\"relationType\" = " + RelationType.BLOCK.ordinal();
        String query = "select t.*, u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"Tweet\" t, \"User\" u"
                + " where t.\"parentTweetID\" = " + parentTweetID
                + " and u.\"accountActive\" = true"
                + " and not exists (" + subQuery2 + ")"
                + " and (u.\"accountPrivate\" = false"
                + " or (u.\"accountPrivate\" = true"
                + " and exists (" + subQuery1 + ")))";
        if(lastTweetDateTime !=null) {
            query += (" and t.\"tweetDateTime\" < '"
                    + lastTweetDateTime.toLocalDate().toString()
                    + "T" + lastTweetDateTime.toLocalTime().toString() + "'");
        }
        query += (" and t.\"userID\" = u.\"userID\" "
                + "order by t.\"tweetDateTime\" desc "
                + "limit " + maxNum);
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendTweetList(rs,rp,PacketType.GET_COMMENTS_RES);
    }

    public void handleGetTweetListReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",", -1);
        int maxNum = Integer.parseInt(args[0]);
        LocalDateTime lastTweetDateTime = null;
        if(!args[1].isEmpty())
            lastTweetDateTime = LocalDateTime.parse(args[1]);
        /***************************************************************************************/
        String query = "select t.*, u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"Tweet\" t, \"User\" u"
                + " where t.\"userID\" = " + userID;
        if(lastTweetDateTime !=null) {
            query += (" and t.\"tweetDateTime\" < '"
                    + lastTweetDateTime.toLocalDate().toString()
                    + "T" + lastTweetDateTime.toLocalTime().toString() + "'");
        }
        query += (" and t.\"userID\" = u.\"userID\" "
                + "order by t.\"tweetDateTime\" desc "
                + "limit " + maxNum);
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        /***************************************************************************************/
        sendTweetList(rs,rp,PacketType.GET_TWEET_LIST_RES);
    }

    public static void sendTweetList(ResultSet rs, Packet request, PacketType packetType){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(request.getClientID()).getUserID();

        try {
            ClientHandler clt = socketController.getClient(request.getClientID());
            String body = "";
            int likedNum=0, commentNum=0;
            boolean youLiked = false;
            while(rs.next()){
                likedNum = TweetController.getLikedNum(rs.getLong("tweetID"));
                commentNum = TweetController.getCommentNum(rs.getLong("tweetID"));
                youLiked = TweetController.getYouLiked(userID,rs.getLong("tweetID"));
                /************************************/
                String tweetImageStr = "", userImageStr = "";
                byte[] tweetImage = rs.getBytes("tweetImage");
                byte[] userImage = rs.getBytes("userImage");
                if(tweetImage!=null)
                    tweetImageStr = Base64.getEncoder().encodeToString(tweetImage);
                if(userImage!=null)
                    userImageStr = Base64.getEncoder().encodeToString(userImage);

                body = rs.getString("tweetText") + ","
                        + rs.getTimestamp("tweetDateTime").toLocalDateTime().toString() + ","
                        + rs.getLong("userID") + ","
                        + rs.getLong("tweetID") + ","
                        + rs.getBoolean("retweeted") + ","
                        + youLiked + ","
                        + tweetImageStr + ","
                        + rs.getString("userName") + ","
                        + rs.getString("firstName") + ","
                        + rs.getString("lastName") + ","
                        + userImageStr + ","
                        + likedNum + ","
                        + commentNum;
                clt.addResponse(
                        new Packet(packetType,
                                body,
                                clt.getAuthToken(),
                                true,
                                request.getClientID(),
                                request.getRequestID())
                );
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }

    public static int getLikedNum(long tweetID){
        String query = "select count(*) from \"Like/Spam\""
                + " where \"tweetID\" = " + tweetID
                + " and \"actionType\" = " + ActionType.LIKE.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            return rs.getInt("count");
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    public static int getCommentNum(long tweetID){
        String query = "select count(*) from \"Tweet\""
                + " where \"parentTweetID\" = " + tweetID;
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            return rs.getInt("count");
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return 0;
    }

    public static boolean getYouLiked(long userID,long tweetID){
        String query = "select count(*) from \"Like/Spam\""
                + " where \"tweetID\" = " + tweetID
                + " and \"userID\" = " + userID
                + " and \"actionType\" = " + ActionType.LIKE.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            if(rs.getInt("count")!=0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return false;
    }

    public static boolean getIsMute(long subjectID,long objectID){
        String query = "select count(*) from \"Relation\""
                + " where \"subjectID\" = " + subjectID
                + " and \"objectID\" = " + objectID
                + " and \"relationType\" = " + RelationType.MUTE.ordinal();
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        try {
            rs.next();
            if(rs.getInt("count")!=0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return false;
    }
}
