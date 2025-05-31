import java.io.IOException;
import java.util.logging.*;

public class AppLogger {
    private static final Logger logger = Logger.getLogger(AppLogger.class.getName());

    static {
        try {
            LogManager.getLogManager().reset(); // Clear default handlers

            // Create a file handler
            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Optional: Console output
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());

            // Add handlers
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.INFO); // Set desired log level

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
