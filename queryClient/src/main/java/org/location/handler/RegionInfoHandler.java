package org.location.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public class RegionInfoHandler implements HttpHandler {
    private final DataGenerator dataGenerator;

    public RegionInfoHandler(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            org.location.utils.Log.logger.info("接收到获取区域信息请求");
            System.out.println("[queryClient] /get/regioninfo 请求已接收");
            String response = dataGenerator.getRegionInfoJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("X-Message", "获取区域信息成功");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
            org.location.utils.Log.logger.info("成功返回区域信息");
            System.out.println("[queryClient] /get/regioninfo 返回成功");
        } catch (Exception e) {
            Log.logger.severe("获取区域信息失败: " + e.getMessage());
            String response = e.getMessage();
            exchange.sendResponseHeaders(500, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }
}
