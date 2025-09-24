package org.location.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.DataGenerator;
import org.location.SHE;
import org.location.utils.Info;
import org.location.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * 接收协作云发送的查询结果列表，并将其解密存储
 */
public class AcceptResultAddRndHandler implements HttpHandler {
    private DataGenerator dataGenerator;
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();

    public AcceptResultAddRndHandler(DataGenerator dataGenerator, SHE she) {
        this.dataGenerator = dataGenerator;
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
                Log.logger.info("接收待解密的参数");

                JsonNode infoListNode = jsonNode.get("infoList");
                if (infoListNode.isArray()) {
                    dataGenerator.clear();
                    for (JsonNode infoNode : infoListNode) {
                        BigInteger id = she.SHEDecryption(new BigInteger(infoNode.get("id").asText()));
                        BigInteger x1 = she.SHEDecryption(new BigInteger(infoNode.get("x1").asText()));
                        BigInteger y1 = she.SHEDecryption(new BigInteger(infoNode.get("y1").asText()));
                        BigInteger x1_ = she.SHEDecryption(new BigInteger(infoNode.get("x1_").asText()));
                        BigInteger y1_ = she.SHEDecryption(new BigInteger(infoNode.get("y1_").asText()));
                        Info info = new Info(id, x1, y1, x1_, y1_);
                        dataGenerator.addInfo(info);
                    }
                }
                Log.logger.info("完成解密");

                // 构造响应
                String response = "{\n" +
                        "  \"status\": \"success\",\n" +
                        "  \"message\": \"Data successfully sent and processed.\"\n" +
                        "}";
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
