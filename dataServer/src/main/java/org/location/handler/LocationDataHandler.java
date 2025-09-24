package org.location.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.EncryptedQuadtree;
import org.location.utils.Info;
import org.location.utils.Log;
import org.location.SHE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocationDataHandler implements HttpHandler {
    private EncryptedQuadtree encryptedQuadtree;
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<Info> locationData = new ArrayList<>(2000);

    public LocationDataHandler(EncryptedQuadtree encryptedQuadtree, SHE she) {
        this.encryptedQuadtree = encryptedQuadtree;
        this.she = she;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            // 解析JSON数据
            JsonNode jsonNode = objectMapper.readTree(requestBody.toString());

            try {
                // 处理解析后的JSON数据
                JsonNode indexNode = jsonNode.get("index");
                JsonNode infoNode = jsonNode.get("info");

                List<Integer> index = objectMapper.convertValue(indexNode, new TypeReference<List<Integer>>() {
                });
                BigInteger id = new BigInteger(infoNode.get("id").asText());
                BigInteger x1 = new BigInteger(infoNode.get("x1").asText());
                BigInteger y1 = new BigInteger(infoNode.get("y1").asText());
                BigInteger x1_ = new BigInteger(infoNode.get("x1_").asText());
                BigInteger y1_ = new BigInteger(infoNode.get("y1_").asText());
                Info info = new Info(id, x1, y1, x1_, y1_);
                this.locationData.add(info);
                encryptedQuadtree.Insert(info, index);
                // 简洁日志：仅输出累计条数
                if (this.locationData.size() % 500 == 0)
                    Log.logger.info(String.format("位置点插入完成，当前累计条数：%d", this.locationData.size()));

                // 构造响应
                String response = "Received and parsed JSON data.\n" + requestBody;
                exchange.sendResponseHeaders(200, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
            } catch (Exception ex) {
                // 构造响应
                String response = ex.getMessage();
                exchange.sendResponseHeaders(422, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();

                Log.logger.severe("位置点信息无法解析");
            }
        } else if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Log.logger.info("接收到密文位置信息请求");

                // 构造json格式数据
                ObjectMapper mapper = new ObjectMapper();
                String response = mapper.writeValueAsString(this.locationData);

                exchange.sendResponseHeaders(200, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
                Log.logger.info("成功返回密文位置信息");
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
