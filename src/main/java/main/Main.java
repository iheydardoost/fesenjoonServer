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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

//        String str = "salam, chetori?, khoobi$, ha$ $vaghean, bashe.";
//        System.out.println(str);
//        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
//        String encodedStr = Base64.getEncoder().encodeToString(bytes);
//        System.out.println(encodedStr);
//        byte[] bytes1 = Base64.getDecoder().decode(encodedStr);
//        String encodedStr1 = new String(bytes1,StandardCharsets.UTF_8);
//        System.out.println(encodedStr1);


//        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
//        byteBuffer.putLong(1L);
//        byte[] bytes1 = byteBuffer.array();
    }

    private static void initialize(){
        LogHandler.initLogger(false);
        JsonHandler.initMapper();
    }

    public static MainController getMainController() {
        return mainController;
    }
}
