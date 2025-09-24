package org.location.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.SHE;
import org.location.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class SHEParameterHandler implements HttpHandler {
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();

    public SHEParameterHandler(SHE she) {
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
                int k0 = Integer.parseInt(jsonNode.get("k0").asText());
                int k1 = Integer.parseInt(jsonNode.get("k1").asText());
                int k2 = Integer.parseInt(jsonNode.get("k2").asText());
                int k3 = Integer.parseInt(jsonNode.get("k3").asText());
                BigInteger p = new BigInteger(jsonNode.get("p").asText());
                BigInteger q = new BigInteger(jsonNode.get("q").asText());
                BigInteger l = new BigInteger(jsonNode.get("l").asText());
                BigInteger N = new BigInteger(jsonNode.get("N").asText());
                BigInteger E0_1 = new BigInteger(jsonNode.get("E0_1").asText());
                BigInteger E0_2 = new BigInteger(jsonNode.get("E0_2").asText());

                Log.logger.info("接收SHE算法参数：");
                Log.logger.info(String.format("k0: %d, k1: %d, k2: %d, k3: %d", k0, k1, k2, k3));
                Log.logger.info(String.format("p: %s, q: %s, l: %s, N: %s",
                        p.toString().substring(0, 5),
                        q.toString().substring(0, 5),
                        l.toString().substring(0, 5),
                        N.toString().substring(0, 5)));
                Log.logger.info(String.format("E0_1: %s, E0_2: %s",
                        E0_1.toString().substring(0, 5),
                        E0_2.toString().substring(0, 5)));

                she.Init(k0, k1, k2, k3, p, q, l, N, E0_1, E0_2);
                Log.logger.config("SHE同态加密算法参数初始化完成");
            } catch (Exception ex) {
                // 构造响应
                String response = ex.getMessage();
                exchange.sendResponseHeaders(422, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
                Log.logger.severe("SHE参数无法解析");
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
