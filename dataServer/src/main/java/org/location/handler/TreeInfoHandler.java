package org.location.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.location.EncryptedQuadtree;
import org.location.utils.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TreeInfoHandler implements HttpHandler {
    private final EncryptedQuadtree encryptedQuadtree;
    private final ObjectMapper mapper = new ObjectMapper();

    public TreeInfoHandler(EncryptedQuadtree encryptedQuadtree) {
        this.encryptedQuadtree = encryptedQuadtree;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Log.logger.info("接收到获取密文四叉树结构请求");
            System.out.println("[dataServer] /get/treeinfo 请求已接收");
            URI uri = exchange.getRequestURI();
            Map<String, String> q = parseQuery(uri.getRawQuery());
            int maxDepth = Integer.parseInt(q.getOrDefault("maxDepth", "4"));
            int maxLeafItems = Integer.parseInt(q.getOrDefault("maxLeafItems", "0"));
            boolean truncate = Boolean.parseBoolean(q.getOrDefault("truncate", "true"));

            // 获取完整密文（不截断），再在本地按 128 字符进行可视化截断
            Object payload = encryptedQuadtree.exportTreeInfo(maxDepth, maxLeafItems, false);
            Object zhPayload = convertToChineseWithTruncate(payload, 256);
            String response = mapper.writeValueAsString(zhPayload);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("X-Message", "获取密文四叉树结构成功");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
            Log.logger.info("成功返回密文四叉树结构摘要");
            System.out.println("[dataServer] /get/treeinfo 返回成功");
        } catch (Exception e) {
            Log.logger.severe("导出密文四叉树失败: " + e.getMessage());
            String response = e.getMessage();
            exchange.sendResponseHeaders(500, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }

    private Map<String, String> parseQuery(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null || raw.isEmpty())
            return m;
        for (String kv : raw.split("&")) {
            int i = kv.indexOf('=');
            if (i > 0)
                m.put(kv.substring(0, i), i + 1 < kv.length() ? kv.substring(i + 1) : "");
        }
        return m;
    }

    // 将导出的结构转换为中文可视化键名，并对长密文进行指定长度截断后加省略号
    @SuppressWarnings("unchecked")
    private Object convertToChineseWithTruncate(Object obj, int maxLen) {
        if (obj == null)
            return null;
        if (obj instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) obj;
            java.util.Map<String, Object> out = new java.util.HashMap<>();

            // 顶层键转换
            if (m.containsKey("MINSIZE") || m.containsKey("tree")) {
                if (m.containsKey("MINSIZE"))
                    out.put("最小叶子边长", truncateVal(m.get("MINSIZE"), maxLen));
                if (m.containsKey("tree"))
                    out.put("密文四叉树", convertToChineseWithTruncate(m.get("tree"), maxLen));
                return out;
            }

            // 节点视图转换
            if (m.containsKey("x1") && m.containsKey("y1") && m.containsKey("x2") && m.containsKey("y2")) {
                out.put("左下角x(密文)", truncateVal(m.get("x1"), maxLen));
                out.put("左下角y(密文)", truncateVal(m.get("y1"), maxLen));
                out.put("右上角x(密文)", truncateVal(m.get("x2"), maxLen));
                out.put("右上角y(密文)", truncateVal(m.get("y2"), maxLen));
                if (m.containsKey("leafSize"))
                    out.put("叶子包含数量", m.get("leafSize"));
                if (m.containsKey("A"))
                    out.put("子节点A", convertToChineseWithTruncate(m.get("A"), maxLen));
                if (m.containsKey("B"))
                    out.put("子节点B", convertToChineseWithTruncate(m.get("B"), maxLen));
                if (m.containsKey("C"))
                    out.put("子节点C", convertToChineseWithTruncate(m.get("C"), maxLen));
                if (m.containsKey("D"))
                    out.put("子节点D", convertToChineseWithTruncate(m.get("D"), maxLen));
                return out;
            }

            // 其他 Map 兜底：逐项转换
            for (Map.Entry<String, Object> e : m.entrySet()) {
                out.put(e.getKey(), convertToChineseWithTruncate(e.getValue(), maxLen));
            }
            return out;
        } else if (obj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) obj;
            java.util.List<Object> out = new java.util.ArrayList<>(list.size());
            for (Object it : list)
                out.add(convertToChineseWithTruncate(it, maxLen));
            return out;
        } else {
            return truncateVal(obj, maxLen);
        }
    }

    private Object truncateVal(Object v, int maxLen) {
        if (!(v instanceof String))
            return v;
        String s = (String) v;
        if (s.length() <= maxLen)
            return s;
        return s.substring(0, maxLen) + "...";
    }
}
