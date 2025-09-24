package org.location.utils;

import org.location.SHE;
import org.location.handler.LogHelperHandler;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log {
    public static final Logger logger = Logger.getLogger(SHE.class.getName());

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
        // 用于记录所有的日志信息
        logger.addHandler(new LogHelperHandler());
    }
}
