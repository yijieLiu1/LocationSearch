package org.location.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.location.SHE;
import org.location.utils.Info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Sender {
    private static String baseUrl = "http://localhost:16789/";
    private static int ciphernum = 0;

    private static void send(String url, String requestBody) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 每次发送计数+1
            ciphernum++;
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

            // 每500条输出一次
            if (ciphernum % 500 == 0) {
                System.out.println("发送密文数：" + ciphernum + " Response Code: " + responseCode);
            }
            // System.out.println("Response Message: " + responseMessage);
            // System.out.println("Response Body: " + response.toString());
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

    public static void sendLocationData(List<Integer> index, Info info) throws IOException {
        String url = baseUrl + "locationData";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putPOJO("index", index);
        root.putPOJO("info", info);
        String requestBody = mapper.writeValueAsString(root);

        send(url, requestBody);
    }

    public static void sendSHEParameter(SHE she) throws IOException {
        String url = baseUrl + "post/SHEParameter";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(she);
        System.out.println(requestBody);

        send(url, requestBody);
    }

    public static void sendQuadtreeParameter(BigInteger sx1, BigInteger sy1, BigInteger sx1_, BigInteger sy1_,
            BigInteger sx2_, BigInteger sy2_,
            BigInteger MINSIZE, BigInteger num) throws IOException {
        String url = baseUrl + "post/quadtreeParameter";

        // 构造json格式数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("sx1", sx1.toString());
        root.put("sy1", sy1.toString());
        root.put("sx1_", sx1_.toString());
        root.put("sy1_", sy1_.toString());
        root.put("sx2_", sx2_.toString());
        root.put("sy2_", sy2_.toString());
        root.put("MINSIZE", MINSIZE.toString());
        root.put("num", num.toString());
        String requestBody = mapper.writeValueAsString(root);

        send(url, requestBody);
    }
}
