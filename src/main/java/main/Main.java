package main;

import controller.JsonHandler;
import controller.LogHandler;
import controller.MainController;

public class Main {
    private static MainController mainController;
    public static void main(String[] args) {
        LogHandler.initLogger(false);
        JsonHandler.initMapper();

        mainController = new MainController();
    }

    public static MainController getMainController() {
        return mainController;
    }
}
