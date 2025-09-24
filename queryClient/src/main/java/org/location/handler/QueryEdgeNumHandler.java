package org.location.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class QueryEdgeNumHandler implements HttpHandler {
    private DataGenerator dataGenerator;

    public QueryEdgeNumHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到多边形边数请求");

                // 构造json格式数据
                ObjectMapper mapper = new ObjectMapper();
                String response = mapper.writeValueAsString(dataGenerator.getEdgeNum());

                exchange.sendResponseHeaders(200, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
                Log.logger.info("成功返回多边形边数");
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
