package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.*;

/**
 * 日志处理Handler
 *
 * 用于接收日志请求，返回所有日志
 */
public class LogHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到日志请求");

                String response = "";
                for (Handler handler : Log.logger.getHandlers()) {
                    if (handler instanceof LogHelperHandler) {
                        LogHelperHandler logHelperHandler = (LogHelperHandler) handler;
                        // 获取缓冲区中的日志内容
                        response = logHelperHandler.getLogMessages();
                    }
                }
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes("UTF-8"));
                outputStream.close();
                Log.logger.info("成功返回日志");
            } catch (Exception ex) {
                // 构造响应
                String response = ex.getMessage();
                exchange.sendResponseHeaders(500, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();

                Log.logger.severe("遇到意外错误：" + ex.getMessage());
            }
        } else {
            // 其他请求方法的处理
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"

            Log.logger.warning("错误的请求方法");
        }
    }
}
