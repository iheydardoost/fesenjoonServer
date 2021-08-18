package main;

import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;
import controller.JsonHandler;
import controller.LogHandler;
import controller.MainController;
import model.ActionType;
import model.RelationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static controller.MainController.REPORTED_NUMBER_LIMIT;

public class Main {
    private static MainController mainController;

    public static void main(String[] args) {
        initialize();
        mainController = new MainController();
        mainController.getDbCommunicator().initDBCommunicator();
        mainController.getSocketController().initConnection();
    }

    private static void initialize(){
        LogHandler.initLogger(false);
        JsonHandler.initMapper();
    }

    public static MainController getMainController() {
        return mainController;
    }
}
