package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class LinearQueryResultHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public LinearQueryResultHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到线性查询结果请求");

                String response = dataGenerator.getLinearResultStr();

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.getResponseHeaders().set("X-Message", "获取线性查询结果成功");
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes("UTF-8"));
                outputStream.close();
                Log.logger.info("成功返回线性查询结果");
            } catch (Exception ex) {
                String response = ex.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();

                Log.logger.severe("线性查询结果遇到意外错误：" + ex.getMessage());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"
            Log.logger.warning("错误的请求方法");
        }
    }
}
