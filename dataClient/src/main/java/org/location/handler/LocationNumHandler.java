package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class LocationNumHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public LocationNumHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到位置点数量请求");
                // 构造响应
                String response = String.valueOf(dataGenerator.getLocationNum());
                exchange.sendResponseHeaders(200, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
                Log.logger.info("成功返回位置点数量");
            } catch (Exception ex) {
                // 构造响应
                String response = ex.getMessage();
                exchange.sendResponseHeaders(500, response.length());
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
