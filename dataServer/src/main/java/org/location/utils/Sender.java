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
    private static String baseUrl = "http://localhost:26789/";

    private static String send(String url, String requestBody) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 创建URL对象
            URL apiUrl = new URL(url);

            // 创建HttpURLConnection对象
            connection = (HttpURLConnection) apiUrl.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 设置请求体数据
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] requestBodyBytes = requestBody.getBytes("UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));

            // 发送请求体数据
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBodyBytes);
            outputStream.close();

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
//            System.out.println("Response Code: " + responseCode);
//            System.out.println("Response Message: " + responseMessage);
//            System.out.println("Response Body: " + response.toString());

            if (responseCode == 200) {
                return response.toString();
            } else {
                throw new CollaborationException(response.toString());
            }
        } catch (CollaborationException e) {
            throw new Exception(e);
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


    public static BigInteger sendForComparison(BigInteger value) throws Exception {
        String url = baseUrl + "post/comparison";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("value", value.toString());
        String requestBody = mapper.writeValueAsString(root);

        Log.logger.info("发送协作解密请求");
        String response = send(url, requestBody);
        Log.logger.info("协作解密成功");

        return new BigInteger(response);
    }

    public static BigInteger sendForMultiply(BigInteger value1, BigInteger value2) throws Exception {
        String url = baseUrl + "post/multiply";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("value1", value1.toString());
        root.put("value2", value2.toString());
        String requestBody = mapper.writeValueAsString(root);

        Log.logger.info("发送协作乘法请求");
        String response = send(url, requestBody);
        Log.logger.info("协作乘法成功");

        return new BigInteger(response);
    }

    public static void sendResultAddRnd(List<Info> resultAddRnd) throws Exception {
        String url = baseUrl + "post/acceptResultAddRnd";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putPOJO("infoList", resultAddRnd);
        String requestBody = mapper.writeValueAsString(root);

        Log.logger.info("发送加扰后的查询结果");
        send(url, requestBody);
        Log.logger.info("发送成功");
    }
}
