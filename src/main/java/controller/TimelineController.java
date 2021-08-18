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
        TweetController.sendTweetList(rs,rp,PacketType.TIMELINE_TWEET_RES);
    }
}
