package controller;

import main.Main;
import model.ActionType;
import model.Packet;
import model.PacketType;

import model.RelationType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

import static controller.MainController.REPORTED_NUMBER_LIMIT;

public class TimelineController {

    public TimelineController() {
    }

    public void handleTimelineTweetReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",", -1);
        int maxNum = Integer.parseInt(args[0]);
        LocalDateTime lastTweetDateTime = null;
        if(!args[1].isEmpty())
            lastTweetDateTime = LocalDateTime.parse(args[1]);
        /***************************************************************************************/
        String subQuery1 = "select * from \"Relation\" rr, \"Like/Spam\" ls"
                + " where rr.\"subjectID\" = " + userID
                + " and (rr.\"objectID\" = t.\"userID\""
                + " or (ls.\"tweetID\" = t.\"tweetID\""
                + " and ls.\"userID\" = rr.\"objectID\""
                + " and ls.\"actionType\" = " + ActionType.LIKE.ordinal() + "))"
                + " and rr.\"relationType\" = " + RelationType.FOLLOW.ordinal();
        String subQuery2 = "select * from \"Relation\" rr"
                + " where rr.\"subjectID\" = " + userID
                + " and rr.\"objectID\" = t.\"userID\""
                + " and (rr.\"relationType\" = " + RelationType.MUTE.ordinal()
                + " or rr.\"relationType\" = " + RelationType.BLOCK.ordinal() + ")";
        String query = "select t.*, u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                + " from \"Tweet\" t, \"User\" u, \"Relation\" r"
                + " where u.\"accountActive\" = true"
                + " and t.\"reportedNumber\" <= " + REPORTED_NUMBER_LIMIT
                + " and exists (" + subQuery1 + ")"
                + " and not exists (" + subQuery2 + ")";
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
        try {
            ClientHandler clt = socketController.getClient(rp.getClientID());
            String body = "";
            int likedNum=0, commentNum=0;
            boolean youLiked = false;
            ResultSet rs1 = null;
            while(rs.next()){
                query = "select count(*) from \"Like/Spam\""
                        + " where \"tweetID\" = " + rs.getLong("tweetID")
                        + " and \"actionType\" = " + ActionType.LIKE.ordinal();
                rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
                rs1.next();
                likedNum = rs1.getInt("count");

                query = "select count(*) from \"Tweet\""
                        + " where \"parentTweetID\" = " + rs.getLong("tweetID");
                rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
                rs1.next();
                commentNum = rs1.getInt("count");

                query = "select count(*) from \"Like/Spam\""
                        + " where \"tweetID\" = " + rs.getLong("tweetID")
                        + " and \"userID\" = " + userID
                        + " and \"actionType\" = " + ActionType.LIKE.ordinal();
                rs1 = Main.getMainController().getDbCommunicator().executeQuery(query);
                rs1.next();
                if(rs1.getInt("count")!=0)
                    youLiked = true;
                else
                    youLiked = false;
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
                        new Packet(PacketType.TIMELINE_TWEET_RES,
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
}
