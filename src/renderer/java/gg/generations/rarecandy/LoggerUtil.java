package gg.generations.rarecandy;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static Logger LOGGER;

    public static void printError(Exception e) {
        checkLogger();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        LOGGER.severe(sw.toString());
    }

    public static void print(String string) {
        checkLogger();
        LOGGER.info(string);
    }

    public static void print(long bytesWritten) {
        print(Long.toString(bytesWritten));
    }

    public static void printError(String string) {
        checkLogger();
        LOGGER.severe(string);
    }

    private static void checkLogger() {
        if(LOGGER == null) {
            LOGGER = Logger.getLogger(LoggerUtil.class.getName());
            configureLogging();
        }
    }

    private static void configureLogging() {
        try {
            // Get the root logger
            Logger rootLogger = Logger.getLogger("");

            // Remove existing handlers (optional, if you want to start with a clean slate)
            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // Create a ConsoleHandler and set its level and formatter
            java.util.logging.ConsoleHandler consoleHandler = new java.util.logging.ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleLogFormatter());

            // Ensure the logs directory is created
            createLogsDirectory();

            // Create a FileHandler with a dynamic log file name and set its level and formatter
            String logFilePath = getDynamicLogFilePath();
            FileHandler fileHandler = new FileHandler(logFilePath, true); // Append to the log file
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleLogFormatter());

            // Add both handlers to the root logger
            rootLogger.addHandler(consoleHandler);
            rootLogger.addHandler(fileHandler);

            // Set the global logging level
            rootLogger.setLevel(Level.INFO);
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }

    private static void createLogsDirectory() throws IOException {
        // Check if the logs directory exists, and create it if it doesn't
        Path logs = Paths.get("logs");
        if (!Files.exists(logs)) {
            Files.createDirectories(logs);
        }
    }

    private static String getDynamicLogFilePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = sdf.format(new Date());
        return "logs/" + formattedDate + ".log";
    }

    private static class SimpleLogFormatter extends SimpleFormatter {
        private static final String LOG_FORMAT = "[%2$s] [%1$tF %1$tT]: %3$s%n";

        @Override
        public synchronized String format(java.util.logging.LogRecord record) {
            return String.format(LOG_FORMAT, new Date(record.getMillis()), record.getLevel(), record.getMessage());
        }
    }
}
