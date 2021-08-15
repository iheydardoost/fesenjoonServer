package controller;

import org.apache.log4j.*;

import static controller.ConsoleColors.*;

public class LogHandler {
    public final static Logger logger = Logger.getLogger(LogHandler.class.getName());
    private final static ConsoleAppender consoleAppender = new ConsoleAppender();
    private final static FileAppender fileAppender = new FileAppender();

    public LogHandler() {
    }

    public static void initLogger(boolean useFileAppender){
        if(useFileAppender) initFileAppender();
        else initConsoleAppender();
    }

    private static void initConsoleAppender(){
        consoleAppender.setThreshold(Level.ALL);
        consoleAppender.setImmediateFlush(true);
        String pattern = YELLOW + "[%d]" + BLUE_BOLD + " {%p}" + GREEN_BOLD + " <%C{1}>" + COLOR_RESET + " : %m%n";
        consoleAppender.setLayout(new PatternLayout(pattern));
        consoleAppender.activateOptions();
        logger.addAppender(consoleAppender);
    }

    private static void initFileAppender(){
        fileAppender.setThreshold(Level.INFO);
        fileAppender.setImmediateFlush(true);
        fileAppender.setFile("./fsjLog.log");
        String pattern = "[%d] {%p | %C{1}} : %m%n";
        fileAppender.setLayout(new PatternLayout(pattern));
        fileAppender.activateOptions();
        logger.addAppender(fileAppender);
    }
}

