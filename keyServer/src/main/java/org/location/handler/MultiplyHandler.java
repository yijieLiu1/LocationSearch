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
 * 乘法协作Handler
 *
 * 用于接收两个密文，将密文解密后变成明文，再进行乘法，并将乘法结果加密返回
 */
public class MultiplyHandler implements HttpHandler {
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();
    public MultiplyHandler(SHE she) {
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
            Log.logger.info("接收待进行乘法协作计算的参数");

            // 处理解析后的JSON数据
            try {
                BigInteger value1 = new BigInteger(jsonNode.get("value1").asText());
                BigInteger value2 = new BigInteger(jsonNode.get("value2").asText());

                BigInteger decryptedValue = she.SHEDecryption(value1).multiply(she.SHEDecryption(value2));
                BigInteger newValue = she.SHEPublicKeyEncryption(decryptedValue);
                Log.logger.info("完成乘法协作");
                Log.logger.info(String.format("密文(%s... ,%s...) ===> 明文(%s)",
                        value1.toString().substring(0, 5),
                        value2.toString().substring(0, 5),
                        decryptedValue.toString()));

                // 构造响应
                String response = newValue.toString();
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
                Log.logger.severe("乘法协作参数无法解析");
            }
        } else {
            // 其他请求方法的处理
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"

            Log.logger.warning("错误的请求方法");
        }
    }
}
