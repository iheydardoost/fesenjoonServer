package controller;

import main.Main;
import model.ActionType;
import model.Packet;
import model.PacketType;
import model.RelationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

import static controller.MainController.REPORTED_NUMBER_LIMIT;

public class ExplorerController {

    public ExplorerController() {
    }

    public void handleExplorerTweetReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = socketController.getClient(rp.getClientID()).getUserID();

        String[] args = rp.getBody().split(",", -1);
        int maxNum = Integer.parseInt(args[0]);
        LocalDateTime lastTweetDateTime = null;
        if(!args[1].isEmpty())
            lastTweetDateTime = LocalDateTime.parse(args[1]);
        /***************************************************************************************/
        String subQuery = "select * from \"Relation\" rr"
                        + " where rr.\"subjectID\" = " + userID
                        + " and rr.\"objectID\" = t.\"userID\""
                        + " and (rr.\"relationType\" = " + RelationType.MUTE.ordinal()
                        + " or rr.\"relationType\" = " + RelationType.BLOCK.ordinal() + ")";
        String query = "select t.*, u.\"userName\", u.\"firstName\", u.\"lastName\", u.\"userImage\""
                    + " from \"Tweet\" t, \"User\" u, \"Relation\" r"
                    + " where u.\"accountPrivate\" = false"
                    + " and u.\"accountActive\" = true"
                    + " and t.\"reportedNumber\" <= " + REPORTED_NUMBER_LIMIT
                    + " and not exists (" + subQuery + ")";
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
        TweetController.sendTweetList(rs,rp,PacketType.EXPLORER_TWEET_RES);
    }

    public void handleSearchUsernameReq(Packet rp){
        SocketController socketController = Main.getMainController().getSocketController();
        long subjectID = socketController.getClient(rp.getClientID()).getUserID();
        String userName = rp.getBody();

        String query = "select * from \"User\" u"
                + " where u.\"userName\" = " + userName
                + " and u.\"accountActive\" = true";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);

        ClientHandler clt = socketController.getClient(rp.getClientID());
        try {
            if(rs.next()){
                clt.addResponse(
                        new Packet(PacketType.GET_USER_INFO_RES,
                                "found," + rs.getLong("userID"),
                                clt.getAuthToken(),
                                true,
                                rp.getClientID(),
                                rp.getRequestID())
                );
                return;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        clt.addResponse(
                new Packet(PacketType.SEARCH_USERNAME_RES,
                        "not found,0",
                        clt.getAuthToken(),
                        true,
                        rp.getClientID(),
                        rp.getRequestID())
        );
    }
}
