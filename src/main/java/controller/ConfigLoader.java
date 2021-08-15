package controller;

import model.config.ConfigPaths;
import model.config.DBConnectionConfig;
import model.config.ServerSocketConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConfigLoader {
    private ConfigPaths configPaths;
    private final String CONFIG_PATHS = "./src/main/config/config_paths.json";

    public ConfigLoader() {
        File file = new File(CONFIG_PATHS);
        if(file.isFile()){
            try {
                configPaths = JsonHandler.mapper.readValue(file,ConfigPaths.class);
            } catch (IOException e) {
                //e.printStackTrace();
                LogHandler.logger.fatal("could not read config_paths.json");
            }
        }
        else{
            LogHandler.logger.fatal("config_paths.json does not exist");
        }
    }

    public ServerSocketConfig getServerSocketConfig(){
        ServerSocketConfig serverSocketConfig;
        String path = configPaths.getServerSocketConfigPath();
        File file = new File(path);
        if(file.isFile()) {
            try {
                serverSocketConfig = JsonHandler.mapper.readValue(file,ServerSocketConfig.class);
                LogHandler.logger.info(serverSocketConfig.getIpAddress().getHostAddress()
                        + " / port:" + serverSocketConfig.getPort() + " selected");
                return serverSocketConfig;
            }catch (IOException e) {
                //e.printStackTrace();
                LogHandler.logger.error("could not read server_socket_config.json");
            }
        }
        else {
            LogHandler.logger.error("server_socket_config.json does not exist");
        }

        try {
            serverSocketConfig = new ServerSocketConfig(InetAddress.getByName("localhost"),8000);
            LogHandler.logger.info("localhost / port:8000 selected");
            return serverSocketConfig;
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not connect to Server via localhost and port 8000");
        }
        return null;
    }

    public DBConnectionConfig getDBConnectionConfig(){
        DBConnectionConfig dbConnectionConfig;
        String path = configPaths.getDbConnectionConfigPath();
        File file = new File(path);
        if(file.isFile()) {
            try {
                dbConnectionConfig = JsonHandler.mapper.readValue(file,DBConnectionConfig.class);
                return dbConnectionConfig;
            }catch (IOException e) {
                //e.printStackTrace();
                LogHandler.logger.error("could not read db_connection_config.json");
            }
        }
        else {
            LogHandler.logger.error("db_connection_config.json does not exist");
        }
        return null;
    }
}
