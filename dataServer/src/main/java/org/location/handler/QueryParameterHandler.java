package org.location.handler;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.*;
import org.location.utils.Info;
import org.location.utils.Log;
import org.location.utils.Pair;
import org.location.utils.Sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QueryParameterHandler implements HttpHandler {
    private EncryptedQuadtree encryptedQuadtree;
    private SHE she;
    private ObjectMapper objectMapper = new ObjectMapper();

    public QueryParameterHandler(EncryptedQuadtree encryptedQuadtree, SHE she) {
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

            StreamReadConstraints streamReadConstraints = StreamReadConstraints
                    .builder()
                    .maxNumberLength(Integer.MAX_VALUE)
                    .maxStringLength(Integer.MAX_VALUE)
                    .maxDocumentLength(Integer.MAX_VALUE)
                    .maxNameLength(Integer.MAX_VALUE)
                    .maxNestingDepth(Integer.MAX_VALUE)
                    .build();
            objectMapper.getFactory().setStreamReadConstraints(streamReadConstraints);
            // 解析JSON数据
            JsonNode jsonNode = objectMapper.readTree(requestBody.toString());

            // 处理解析后的JSON数据
            try {
                BigInteger qx1 = new BigInteger(jsonNode.get("qx1").asText());
                BigInteger qy1 = new BigInteger(jsonNode.get("qy1").asText());
                BigInteger qx2 = new BigInteger(jsonNode.get("qx2").asText());
                BigInteger qy2 = new BigInteger(jsonNode.get("qy2").asText());
                BigInteger qx1_ = new BigInteger(jsonNode.get("qx1_").asText());
                BigInteger qy1_ = new BigInteger(jsonNode.get("qy1_").asText());
                BigInteger qx2_ = new BigInteger(jsonNode.get("qx2_").asText());
                BigInteger qy2_ = new BigInteger(jsonNode.get("qy2_").asText());
                boolean rectangleFilterFlag = jsonNode.get("rectangleFilterFlag").asBoolean();

                Log.logger.info("接收查询参数");
                JsonNode pointListNode = jsonNode.get("pointList");
                Log.logger.info(String.format("矩形范围已接收(qx1/qy1/qx2/qy2)，矩形过滤：%s，锚点数：%d",
                        String.valueOf(rectangleFilterFlag),
                        (pointListNode != null && pointListNode.isArray()) ? pointListNode.size() : 0));

                List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList = new ArrayList<>();

                if (pointListNode.isArray()) {
                    for (JsonNode pair : pointListNode) {
                        BigInteger x1 = new BigInteger(pair.get("x1").asText());
                        BigInteger y1 = new BigInteger(pair.get("y1").asText());
                        BigInteger x1_ = new BigInteger(pair.get("x1_").asText());
                        BigInteger y1_ = new BigInteger(pair.get("y1_").asText());

                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(x1, y1, x1_, y1_));
                    }
                }

                double stime = System.currentTimeMillis();
                List<Info> queryResult = encryptedQuadtree.Query(qx1, qy1, qx2, qy2, qx1_, qy1_, qx2_, qy2_, pointList,
                        rectangleFilterFlag, true);
                double etime = System.currentTimeMillis();

                List<Info> resultAddRnd = new ArrayList<>();
                List<Info> rndInfo = new ArrayList<>();
                for (Info info : queryResult) {
                    Random rnd = new Random();
                    BigInteger r_id = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
                    BigInteger id = info.id.add(r_id);

                    BigInteger r_x1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
                    BigInteger x1 = info.x1.add(r_x1);

                    BigInteger r_y1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
                    BigInteger y1 = info.y1.add(r_y1);

                    BigInteger r_x1_ = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
                    BigInteger x1_ = info.x1_.add(r_x1_);

                    BigInteger r_y1_ = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
                    BigInteger y1_ = info.y1_.add(r_y1_);

                    rndInfo.add(new Info(r_id, r_x1, r_y1, r_x1_, r_y1_));
                    resultAddRnd.add(new Info(id, x1, y1, x1_, y1_));
                }

                // 将带扰动的加密查询结果发送给计算协作云
                Log.logger.info("发送带扰动的加密查询结果给计算协作云（省略具体密文）");
                Sender.sendResultAddRnd(resultAddRnd);
                Log.logger.info("发送成功");

                Log.logger.info(
                        String.format("查询完成，结果数量：%d，耗时：%s", queryResult.size(), String.valueOf(etime - stime) + " ms"));

                // 构造json格式数据
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode root = mapper.createObjectNode();
                root.put("query time", String.valueOf(etime - stime) + " ms");
                root.putPOJO("query rnd", rndInfo);

                // 构造响应
                String response = mapper.writeValueAsString(root);
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

                Log.logger.severe("查询请求参数无法解析");
            }
        } else {
            // 其他请求方法的处理
            exchange.sendResponseHeaders(405, -1); // 返回 "Method Not Allowed"

            Log.logger.warning("错误的请求方法");
        }
    }
}
