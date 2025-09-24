package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class SendQueryLinearHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public SendQueryLinearHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到线性查询发送请求");
                String time = dataGenerator.sendQueryLinear();

                String response = "{\n" +
                        "  \"状态\": \"成功\",\n" +
                        "  \"消息\": \"线性查询已完成\",\n" +
                        "  \"耗时\": \"" + time + "\"\n" +
                        "}";
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes("UTF-8"));
                outputStream.close();
                Log.logger.info("成功发送线性查询请求");
            } catch (Exception ex) {
                String response = ex.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();

                Log.logger.severe("线性查询遇到意外错误：" + ex.getMessage());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
            Log.logger.warning("错误的请求方法");
        }
    }
}
