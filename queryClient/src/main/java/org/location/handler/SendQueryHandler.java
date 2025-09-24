package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class SendQueryHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public SendQueryHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到查询发送请求");
                dataGenerator.sendQuery();

                String response = "{\n" +
                        "  \"状态\": \"成功\",\n" +
                        "  \"消息\": \"查询已完成\",\n" +
                        "  \"耗时\": \"" + dataGenerator.getTimeStr() + "\"\n" +
                        "}";
                exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes("UTF-8"));
                outputStream.close();
                Log.logger.info("成功发送查询请求");

                Log.logger.info(String.format("此次查询结果如下\n%s", dataGenerator.getQueryResultStr()));
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
