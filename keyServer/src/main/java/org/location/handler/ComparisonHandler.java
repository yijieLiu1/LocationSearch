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

/**
 * 处理比较协作，对于协作方发送过来的密文，会进行解密并返回解密值和0比较的结果
 *
 * 1. < 0 时返回 -1
 * 2. == 0 时返回 0
 * 3. > 0 时返回 1
 */
public class ComparisonHandler implements HttpHandler {
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();

    public ComparisonHandler(SHE she) {
        this.she = she;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            // 解析JSON数据
            JsonNode jsonNode = objectMapper.readTree(requestBody.toString());

            // 处理解析后的JSON数据
            try {
                BigInteger value = new BigInteger(jsonNode.get("value").asText());

                Log.logger.info("接收待进行比较协作计算的参数");
                BigInteger decryptedValue = she.SHEDecryption(value);
                Log.logger.info(String.format("密文(%s...) ===> 明文(%s)", value.toString().substring(0, 5), decryptedValue.toString()));
                Log.logger.info("完成比较协作");

                // 构造响应
                String response = String.valueOf(decryptedValue.compareTo(new BigInteger("0")));
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
                Log.logger.severe("待进行比较协作计算的参数无法解析");
            }
        } else {
            // 其他请求方法的处理
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"

            Log.logger.warning("错误的请求方法");
        }
    }
}
