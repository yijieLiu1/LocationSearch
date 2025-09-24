package org.location.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Sender {
    private static String baseUrl = "http://localhost:16789/";

    private static String send(String url, String requestBody, String method) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 创建URL对象
            URL apiUrl = new URL(url);

            // 创建HttpURLConnection对象
            connection = (HttpURLConnection) apiUrl.openConnection();

            // 设置请求方法
            connection.setRequestMethod(method);

            if (method == "POST") {
                connection.setDoOutput(true);

                // 设置请求体数据
                connection.setRequestProperty("Content-Type", "application/json");
                byte[] requestBodyBytes = requestBody.getBytes("UTF-8");
                connection.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));

                // 发送请求体数据
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBodyBytes);
                outputStream.close();
            }
            // 获取响应
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 输出响应结果
            // System.out.println("Response Code: " + responseCode);
            // System.out.println("Response Message: " + responseMessage);
            // System.out.println("Response Body: " + response.toString());

            if (responseCode == 200) {
                return response.toString();
            } else {
                throw new QueryException(response.toString());
            }
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            // 关闭连接和读取器
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String sendQueryParameter(BigInteger qx1, BigInteger qy1, BigInteger qx2, BigInteger qy2,
            BigInteger qx1_, BigInteger qy1_, BigInteger qx2_, BigInteger qy2_,
            List<AnchorPoint.Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList,
            boolean rectangleFilterFlag) throws Exception {
        String url = baseUrl + "post/queryParameter";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("qx1", qx1.toString());
        root.put("qy1", qy1.toString());
        root.put("qx2", qx2.toString());
        root.put("qy2", qy2.toString());
        root.put("qx1_", qx1_.toString());
        root.put("qy1_", qy1_.toString());
        root.put("qx2_", qx2_.toString());
        root.put("qy2_", qy2_.toString());
        root.putPOJO("pointList", pointList);
        root.put("rectangleFilterFlag", rectangleFilterFlag);
        String requestBody = mapper.writeValueAsString(root);

        return send(url, requestBody, "POST");
    }

    public static String sendGetResultAddRnd() throws Exception {
        String url = "http://localhost:26789/get/resultAddRnd";

        return send(url, "", "GET");
    }

    public static String sendQueryParameterLinear(BigInteger qx1, BigInteger qy1, BigInteger qx2, BigInteger qy2,
            BigInteger qx1_, BigInteger qy1_, BigInteger qx2_, BigInteger qy2_,
            List<AnchorPoint.Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList,
            boolean rectangleFilterFlag) throws Exception {
        String url = baseUrl + "post/queryParameterLinear";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("qx1", qx1.toString());
        root.put("qy1", qy1.toString());
        root.put("qx2", qx2.toString());
        root.put("qy2", qy2.toString());
        root.put("qx1_", qx1_.toString());
        root.put("qy1_", qy1_.toString());
        root.put("qx2_", qx2_.toString());
        root.put("qy2_", qy2_.toString());
        root.putPOJO("pointList", pointList);
        root.put("rectangleFilterFlag", rectangleFilterFlag);
        String requestBody = mapper.writeValueAsString(root);

        return send(url, requestBody, "POST");
    }
}
