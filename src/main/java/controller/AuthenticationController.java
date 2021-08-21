package controller;

import controller.builder.UserBuilder;
import main.Main;
import model.ChatType;
import model.Packet;
import model.PacketType;
import model.User;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;

public class AuthenticationController {
    private UserBuilder userBuilder;
    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AuthenticationController() {
        userBuilder = new UserBuilder();
    }

    public void handleSignUp(Packet rp){
        String[] bodyArgs = rp.getBody().split(",",-1);
//        for (int i = 0; i < bodyArgs.length; i++) {
//            System.out.println(i + ": " + bodyArgs[i]);
//        }
        String firstName = PacketHandler.getDecodedArg(bodyArgs[0]);
        String lastName = PacketHandler.getDecodedArg(bodyArgs[1]);
        String userName = PacketHandler.getDecodedArg(bodyArgs[2]);
        String password = PacketHandler.getDecodedArg(bodyArgs[3]);
        LocalDate birthDate = null;
        if(!bodyArgs[4].equals("null"))
            birthDate = LocalDate.parse(bodyArgs[4]);
        String email = PacketHandler.getDecodedArg(bodyArgs[5]);
        String phoneNumber = PacketHandler.getDecodedArg(bodyArgs[6]);
        String bio = PacketHandler.getDecodedArg(bodyArgs[7]);
        for(int i=8; i<bodyArgs.length; i++){
            bio += ("," + bodyArgs[i]);
        }
        /**********************************************/
        String query1 = "select count(u) from \"User\" u where u.\"userName\" = '" + userName + "'";
        String query2 = "select count(u) from \"User\" u where u.\"email\" = '" + email + "'";
        int count1=0, count2=0 ;
        ResultSet rs;
        try {
            rs = Main.getMainController().getDbCommunicator().executeQuery(query1);
            rs.next();
            count1 = rs.getInt(1);
            rs.close();
            rs = Main.getMainController().getDbCommunicator().executeQuery(query2);
            rs.next();
            count2 = rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        }

        SocketController socketController = Main.getMainController().getSocketController();
        ClientHandler clt = socketController.getClient(rp.getClientID());
        if(count1 != 0){
            clt.addResponse(
                            new Packet(PacketType.SIGN_IP_RES,
                                    "error,userName already exists",
                                    0,
                                    false,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            return;
        }
        else if(count2 != 0){
            clt.addResponse(
                            new Packet(PacketType.SIGN_IP_RES,
                                    "error,email already exists",
                                    0,
                                    false,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            return;
        }
        /**********************************************/
        User user =
                userBuilder.setFirstName(firstName)
                        .setLastName(lastName)
                        .setUserName(userName)
                        .setPassword(password)
                        .setDateOfBirth(birthDate)
                        .setEmail(email)
                        .setPhoneNumber(phoneNumber)
                        .setBio(bio)
                        .build();

        String query = "insert into \"User\""
                + " ("
                + "\"userID\"" + ","
                + "\"firstName\"" + ","
                + "\"lastName\"" + ","
                + "\"userName\"" + ","
                + "\"passwordHash\"" + ","
                + "\"dateOfBirth\"" + ","
                + "\"email\"" + ","
                + "\"phoneNumber\"" + ","
                + "\"bio\"" + ","
                + "\"lastSeenStatus\"" + ","
                + "\"lastSeen\"" + ","
                + "\"accountPrivate\"" + ","
                + "\"accountActive\""
                + ") "
                + "values"
                + " ("
                + user.getUserID() + ","
                + "'" + user.getFirstName() + "'" + ","
                + "'" + user.getLastName() + "'" + ","
                + "'" + user.getUserName() + "'" + ","
                + user.getPasswordHash() + ",";
        if(user.getDateOfBirth()==null)
            query += (user.getDateOfBirth() + ",");
        else
            query += ("'" + user.getDateOfBirth() + "'" + ",");
        query += ( "'" + user.getEmail() + "'" + ","
                + "'" + user.getPhoneNumber() + "'" + ","
                + "'" + user.getBio() + "'" + ","
                + user.getLastSeenStatus().ordinal() + ","
                + "'" + user.getLastSeen() + "'" + ","
                + user.isAccountPrivate() + ","
                + user.isAccountActive()
                + ")");
        int updatedRowsNum = Main.getMainController().getDbCommunicator().executeUpdate(query);
        /**********************************************/
        if(updatedRowsNum==1){
            int newAuthToken = SECURE_RANDOM.nextInt(Integer.MAX_VALUE);
            clt.setAuthToken(newAuthToken)
                    .setUserID(user.getUserID());
            clt.addResponse(
                            new Packet(PacketType.SIGN_IP_RES,
                                    "success,signed up successfully",
                                    newAuthToken,
                                    true,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            createMessagingDefaults(user);
            LogHandler.logger.info("userID:" + user.getUserID() + " signed up successfully");
        }
        else{
            clt.addResponse(
                            new Packet(PacketType.SIGN_IP_RES,
                                    "error,sign up failed (try again)",
                                    0,
                                    false,
                                    rp.getClientID(),
                                    rp.getRequestID())
                    );
            LogHandler.logger.error("could not create new user in DB");
        }
    }

    private void createMessagingDefaults(User user){
        CollectionController.insertCollection(user.getUserID(), "all");
        long chatID = ChatController.insertChat("savedMessages", ChatType.SAVED_MESSAGES);
        ChatController.insertChatMember(chatID,user.getUserID());
    }

    public void handleLogIn(Packet rp){
//        System.out.println("in handle login");
        String[] bodyArgs = rp.getBody().split(",",-1);
        String userName = PacketHandler.getDecodedArg(bodyArgs[0]);
        int passwordHash = PacketHandler.getDecodedArg(bodyArgs[1]).hashCode();
        /**********************************************/
        String query = "select * from \"User\" u where u.\"userName\" = '" + userName + "'";
        ResultSet rs = Main.getMainController().getDbCommunicator().executeQuery(query);
        boolean userExisted = false;
        try {
            userExisted = rs.next();
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        /**********************************************/
        SocketController socketController = Main.getMainController().getSocketController();
        long userID = 0;
        try {
            if(userExisted){
//                System.out.println("user existed");
                userID = rs.getLong("userID");
                if(socketController.isUserOnline(userID)){
                    socketController.getClient(rp.getClientID())
                            .addResponse(
                                    new Packet(PacketType.LOG_IN_RES,
                                            "error,user is online now",
                                            0,
                                            false,
                                            rp.getClientID(),
                                            rp.getRequestID())
                            );
                    rs.close();
                    return;
                }
            }
            else {
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LOG_IN_RES,
                                        "error,userName does not exist",
                                        0,
                                        false,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                rs.close();
                return;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
        /**********************************************/
        try {
            int dbPasswordHash = rs.getInt("passwordHash");
//            System.out.println(dbPasswordHash);
            if(dbPasswordHash == passwordHash){
                int newAuthToken = SECURE_RANDOM.nextInt(Integer.MAX_VALUE);
                socketController.getClient(rp.getClientID())
                        .setAuthToken(newAuthToken)
                        .setUserID(userID);
                MessageController.setSentMessagesToReceived(userID);
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LOG_IN_RES,
                                        "success,logged in successfully",
                                        newAuthToken,
                                        true,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
                LogHandler.logger.info("userID:" + userID + " logged in successfully");
            }
            else{
//                System.out.println("pass incorrect");
                socketController.getClient(rp.getClientID())
                        .addResponse(
                                new Packet(PacketType.LOG_IN_RES,
                                        "error,password is incorrect",
                                        0,
                                        false,
                                        rp.getClientID(),
                                        rp.getRequestID())
                        );
            }
            rs.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not get result from DB");
        }
    }
}
