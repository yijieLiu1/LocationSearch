package org.location.utils;

import org.location.handler.LogHelperHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log {
    public static final Logger logger = Logger.getLogger("keyServer");

    static {
        if (System.getProperty("java.util.logging.config.file") == null &&
                System.getProperty("java.util.logging.config.class") == null) {
            try {
                LogManager.getLogManager().readConfiguration(
                        Log.class.getClassLoader().getResourceAsStream("log.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.addHandler(new LogHelperHandler());
    }
}
