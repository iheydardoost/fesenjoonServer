package controller;

import main.Main;
import model.config.DBConnectionConfig;

import java.sql.*;

public class DBCommunicator{
    private Connection connection;
    private final Object CONNECTION_LOCK = new Object();
    private DBConnectionConfig dbConnectionConfig;
    private boolean connected;

    public DBCommunicator() {
    }

    public void initDBCommunicator(){
        this.dbConnectionConfig = Main.getMainController().getConfigLoader().getDBConnectionConfig();
        connectDB();
    }

    public boolean connectDB(){
        synchronized (CONNECTION_LOCK) {
            try {
                this.connection =
                        DriverManager.getConnection(
                                dbConnectionConfig.getUrl(),
                                dbConnectionConfig.getUser(),
                                dbConnectionConfig.getPassword());
                connection.setAutoCommit(true);
                this.connected = true;
                LogHandler.logger.info("Connected to the postgreSQL database successfully");
            } catch (SQLException e) {
                //e.printStackTrace();
                this.connected = false;
                LogHandler.logger.error("could not connect to postgreSQL database");
            }
        }
        return connected;
    }

    public ResultSet executeQuery(String query){
        if(connected && connection!=null) {
            synchronized (CONNECTION_LOCK) {
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    statement.close();
                    return resultSet;
                } catch (SQLException e) {
                    //e.printStackTrace();
                    LogHandler.logger.error("SQLException in executeQuery: " + e.getMessage());
                }
            }
        }
        return null;
    }

    public boolean execute(String query){
        if(connected && connection!=null) {
            synchronized (CONNECTION_LOCK) {
                try {
                    Statement statement = connection.createStatement();
                    boolean result = statement.execute(query);
                    statement.close();
                    return result;
                } catch (SQLException e) {
                    //e.printStackTrace();
                    LogHandler.logger.error("SQLException in execute: " + e.getMessage());
                }
            }
        }
        return false;
    }

    public int executeUpdate(String query){
        if(connected && connection!=null) {
            synchronized (CONNECTION_LOCK) {
                try {
                    Statement statement = connection.createStatement();
                    int result = statement.executeUpdate(query);
                    statement.close();
                    return result;
                } catch (SQLException e) {
                    //e.printStackTrace();
                    LogHandler.logger.error("SQLException in executeUpdate: " + e.getMessage());
                }
            }
        }
        return -1;
    }

    public void closeConnection(){
        if(connected && connection!=null) {
            synchronized (CONNECTION_LOCK) {
                try {
                    connection.close();
                    connected = false;
                } catch (SQLException e) {
                    //e.printStackTrace();
                    LogHandler.logger.error("could not close dbConnection: " + e.getMessage());
                }
            }
        }
    }

    public boolean isConnectionValid(){
        if(connection!=null) {
            synchronized (CONNECTION_LOCK) {
                try {
                    boolean result = connection.isValid(2);
                    return result;
                } catch (SQLException e) {
                    //e.printStackTrace();
                    LogHandler.logger.error("could not check dbConnection Validity");
                }
            }
        }
        return false;
    }
}
