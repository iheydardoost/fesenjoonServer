package controller;

import model.Packet;

import java.security.SecureRandom;
import java.sql.*;

public class MainController {
    private SocketController socketController;
    private static SecureRandom SECURE_RANDOM = new SecureRandom();

//    private static EntityManagerFactory ENTITY_MANAGER_FACTORY =
//            Persistence.createEntityManagerFactory("fesenjoonServer");

    public MainController() {
        socketController = new SocketController();

        String query = "select max(\"userID\") from \"User\"";

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "admin";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            System.out.println(resultSet.getInt(1));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void authenticationReq(Packet rp){
        String[] bodyArgs = rp.getBody().split(",");

//        if(bodyArgs[0].equals("SIGNUP")) {
//            LinkedList<User> users = context.userDB.getAll();
//            for (User u : users) {
//                if (u.getUserName().equals(bodyArgs[1])) {
//                    socketController.getClient(rp.getClientID()).addResponse(
//                            new Packet(PacketType.AUTHENTICATION_ERROR,
//                                    "userName Already exists.",
//                                    0,
//                                    false,
//                                    rp.getClientID(),
//                                    rp.getRequestID()));
//                    return;
//                }
//            }
//            User newUser = new User(bodyArgs[1],bodyArgs[2]);
//            context.userDB.add(newUser);
//            int newAuthToken = secureRandom.nextInt();
//            socketController.getClient(rp.getClientID()).setAuthToken(newAuthToken);
//            socketController.getClient(rp.getClientID()).addResponse(
//                    new Packet(PacketType.AUTHENTICATION_SUCCESS,
//                            "signed up.",
//                            newAuthToken,
//                            true,
//                            rp.getClientID(),
//                            rp.getRequestID()));
//            socketController.getClient(rp.getClientID()).setUserID(newUser.getUserID());
//            return;
//        }
//        if(bodyArgs[0].equals("LOGIN")) {
//            LinkedList<User> users = context.userDB.getAll();
//            for (User u : users) {
//                if (u.getUserName().equals(bodyArgs[1])) {
//                    if(socketController.isUserOnline(u.getUserID())){
//                        socketController.getClient(rp.getClientID()).addResponse(
//                                new Packet(PacketType.AUTHENTICATION_ERROR,
//                                        "user is online now.",
//                                        0,
//                                        false,
//                                        rp.getClientID(),
//                                        rp.getRequestID()));
//                        return;
//                    }
//                    if(u.getPass().equals(bodyArgs[2])){
//                        int newAuthToken = secureRandom.nextInt();
//                        socketController.getClient(rp.getClientID()).setAuthToken(newAuthToken);
//                        socketController.getClient(rp.getClientID()).addResponse(
//                                new Packet(PacketType.AUTHENTICATION_SUCCESS,
//                                        "logged in.",
//                                        newAuthToken,
//                                        true,
//                                        rp.getClientID(),
//                                        rp.getRequestID()));
//                        socketController.getClient(rp.getClientID()).setUserID(u.getUserID());
//                        return;
//                    }
//                    socketController.getClient(rp.getClientID()).addResponse(
//                            new Packet(PacketType.AUTHENTICATION_ERROR,
//                                    "pass is wrong.",
//                                    0,
//                                    false,
//                                    rp.getClientID(),
//                                    rp.getRequestID()));
//                    return;
//                }
//            }
//            socketController.getClient(rp.getClientID()).addResponse(
//                    new Packet(PacketType.AUTHENTICATION_ERROR,
//                            "user does not exist.",
//                            0,
//                            false,
//                            rp.getClientID(),
//                            rp.getRequestID()));
//        }
    }

    public SocketController getSocketController() {
        return socketController;
    }
}