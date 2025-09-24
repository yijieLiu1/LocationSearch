package org.location.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.EncryptedQuadtree;
import org.location.utils.Log;
import org.location.SHE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class QuadtreeParameterHandler implements HttpHandler {
    private EncryptedQuadtree encryptedQuadtree;
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();

    public QuadtreeParameterHandler(EncryptedQuadtree encryptedQuadtree, SHE she) {
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

            // 处理解析后的JSON数据
            try {
                BigInteger num = new BigInteger(jsonNode.get("num").asText());
                BigInteger sx1 = new BigInteger(jsonNode.get("sx1").asText());
                BigInteger sy1 = new BigInteger(jsonNode.get("sy1").asText());
                BigInteger sx1_ = new BigInteger(jsonNode.get("sx1_").asText());
                BigInteger sy1_ = new BigInteger(jsonNode.get("sy1_").asText());
                BigInteger sx2_ = new BigInteger(jsonNode.get("sx2_").asText());
                BigInteger sy2_ = new BigInteger(jsonNode.get("sy2_").asText());
                BigInteger MINSIZE = new BigInteger(jsonNode.get("MINSIZE").asText());

                Log.logger.info("接收四叉树参数");
                Log.logger.info(String.format("初始化参数概要：num=%s, MINSIZE(密文前缀)=%s...",
                        num.toString(),
                        MINSIZE.toString().substring(0, Math.min(8, MINSIZE.toString().length()))));

                encryptedQuadtree.Init(sx1, sy1, sx1_, sy1_, sx2_, sy2_, MINSIZE, num);
                Log.logger.config("四叉树参数初始化完成");
            } catch (Exception ex) {
                // 构造响应
                String response = ex.getMessage();
                exchange.sendResponseHeaders(422, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();

                Log.logger.severe("四叉树参数无法解析");
            }

            // 构造响应
            String response = "Received and parsed JSON data.\n" + requestBody;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        } else {
            // 其他请求方法的处理
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"

            Log.logger.warning("错误的请求方法");
        }
    }
}
