package org.location.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHelperHandler extends Handler {
    private List<String> logMessages = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage();
        logMessages.add(message);
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public String getLogMessages() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putPOJO("allLog", this.logMessages);
        return mapper.writeValueAsString(root);
    }
}
