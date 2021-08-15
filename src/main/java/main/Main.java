package main;

import controller.JsonHandler;
import controller.LogHandler;
import controller.MainController;

public class Main {
    private static MainController mainController;

    public static void main(String[] args) {
        initialize();
        mainController = new MainController();
    }

    private static void initialize(){
        LogHandler.initLogger(false);
        JsonHandler.initMapper();
    }

    public static MainController getMainController() {
        return mainController;
    }
}
