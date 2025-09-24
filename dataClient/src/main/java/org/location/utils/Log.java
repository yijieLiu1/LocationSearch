package org.location.utils;

import org.location.SHE;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log {
    public static final Logger logger = Logger.getLogger(SHE.class.getName());

    static {
        if (System.getProperty("java.util.logging.config.file") == null &&
                System.getProperty("java.util.logging.config.class") == null) {
            try {
                java.io.InputStream in = Log.class.getClassLoader().getResourceAsStream("log.properties");
                if (in != null) {
                    LogManager.getLogManager().readConfiguration(in);
                } else {
                    LogManager.getLogManager().readConfiguration(new FileInputStream("log.properties"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
