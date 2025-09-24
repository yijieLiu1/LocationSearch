package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class SendDataHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public SendDataHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到数据发送请求");
                dataGenerator.sendLocationData();
                String response = "{\n" +
                        "  \"状态\": \"成功\",\n" +
                        "  \"消息\": \"数据已成功发送并处理\"\n" +
                        "}";
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes("UTF-8"));
                outputStream.close();
                Log.logger.info("成功发送数据");
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
