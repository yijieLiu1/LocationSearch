package org.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.location.utils.AnchorPoint;
import org.location.utils.Info;
import org.location.utils.Sender;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DataGenerator {
    // 明文坐标点
    private BigInteger qx1, qy1, qx2, qy2, qx1_, qy1_, qx2_, qy2_;
    // 密文坐标点
    private BigInteger s_qx1, s_qy1, s_qx2, s_qy2, s_qx1_, s_qy1_, s_qx2_, s_qy2_;
    private int edgeNum;

    List<AnchorPoint.Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList;
    boolean rectangleFilterFlag;

    private String timeStr = "";
    private String queryResultStr = "";
    private String rectangleFilterFlagStr = "";
    private String linearTimeStr = "";
    private String linearResultStr = "";

    public DataGenerator(SHE she) {
        this.edgeNum = 6; // 凸多边形边的数量

        /* 查询请求多边形对应的矩形 */
        this.qx1 = new BigInteger("4255000");
        this.qy1 = new BigInteger("4325000");
        this.qx2 = new BigInteger("4256000");
        this.qy2 = new BigInteger("4326000");
        this.qx1_ = this.qx1.multiply(new BigInteger("-1"));
        this.qy1_ = this.qy1.multiply(new BigInteger("-1"));
        this.qx2_ = this.qx2.multiply(new BigInteger("-1"));
        this.qy2_ = this.qy2.multiply(new BigInteger("-1"));

        this.s_qx1 = she.SHEPublicKeyEncryption(this.qx1);
        this.s_qy1 = she.SHEPublicKeyEncryption(this.qy1);
        this.s_qx2 = she.SHEPublicKeyEncryption(this.qx2);
        this.s_qy2 = she.SHEPublicKeyEncryption(this.qy2);
        this.s_qx1_ = she.SHEPublicKeyEncryption(this.qx1_);
        this.s_qy1_ = she.SHEPublicKeyEncryption(this.qy1_);
        this.s_qx2_ = she.SHEPublicKeyEncryption(this.qx2_);
        this.s_qy2_ = she.SHEPublicKeyEncryption(this.qy2_);

        /* 根据需要的查询多边形边数产生对应的锚点列表，始终将凸多边形内的锚点放在列表第一的位置 */
        this.pointList = new ArrayList<>();
        AnchorPoint.setPointList(she, pointList, this.edgeNum);

        this.rectangleFilterFlag = true;
    }

    public void sendQuery() throws Exception {
        this.timeStr = "";
        this.queryResultStr = "";
        this.rectangleFilterFlagStr = "";
        String response = Sender.sendQueryParameter(qx1, qy1, qx2, qy2, qx1_, qy1_, qx2_, qy2_, pointList,
                rectangleFilterFlag);

        ObjectMapper mapper = new ObjectMapper();
        // 解析JSON数据
        JsonNode jsonNode = mapper.readTree(response);

        this.timeStr = jsonNode.get("query time").asText();

        List<Info> rndInfoList = new ArrayList<>();
        JsonNode rndInfoListNode = jsonNode.get("query rnd");
        for (JsonNode rndInfoNode : rndInfoListNode) {
            BigInteger id = new BigInteger(rndInfoNode.get("id").asText());
            BigInteger x1 = new BigInteger(rndInfoNode.get("x1").asText());
            BigInteger y1 = new BigInteger(rndInfoNode.get("y1").asText());
            BigInteger x1_ = new BigInteger(rndInfoNode.get("x1_").asText());
            BigInteger y1_ = new BigInteger(rndInfoNode.get("y1_").asText());
            Info rndInfo = new Info(id, x1, y1, x1_, y1_);
            rndInfoList.add(rndInfo);
        }

        String resultAddRndStr = Sender.sendGetResultAddRnd();
        // 解析JSON数据
        jsonNode = mapper.readTree(resultAddRndStr);
        List<Info> resultAddRndInfoList = new ArrayList<>();
        JsonNode resultAddRndInfoListNode = jsonNode.get("resultAddRnd");
        for (JsonNode resultAddRndInfoNode : resultAddRndInfoListNode) {
            BigInteger id = new BigInteger(resultAddRndInfoNode.get("id").asText());
            BigInteger x1 = new BigInteger(resultAddRndInfoNode.get("x1").asText());
            BigInteger y1 = new BigInteger(resultAddRndInfoNode.get("y1").asText());
            BigInteger x1_ = new BigInteger(resultAddRndInfoNode.get("x1_").asText());
            BigInteger y1_ = new BigInteger(resultAddRndInfoNode.get("y1_").asText());
            Info resultAddRndInfo = new Info(id, x1, y1, x1_, y1_);
            resultAddRndInfoList.add(resultAddRndInfo);
        }

        List<Info> result = new ArrayList<>();
        for (int i = 0; i < rndInfoList.size(); i++) {
            BigInteger id = resultAddRndInfoList.get(i).id.subtract(rndInfoList.get(i).id);
            BigInteger x1 = resultAddRndInfoList.get(i).x1.subtract(rndInfoList.get(i).x1);
            BigInteger y1 = resultAddRndInfoList.get(i).y1.subtract(rndInfoList.get(i).y1);
            BigInteger x1_ = resultAddRndInfoList.get(i).x1_.subtract(rndInfoList.get(i).x1_);
            BigInteger y1_ = resultAddRndInfoList.get(i).y1_.subtract(rndInfoList.get(i).y1_);
            Info info = new Info(id, x1, y1, x1_, y1_);
            result.add(info);
        }

        // 构造json格式数据
        // ObjectNode root = mapper.createObjectNode();
        // root.putPOJO("result", result);

        this.queryResultStr = mapper.writeValueAsString(result);
        this.rectangleFilterFlagStr = String.valueOf(this.rectangleFilterFlag);
    }

    public String sendQueryLinear() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String response = Sender.sendQueryParameterLinear(qx1, qy1, qx2, qy2, qx1_, qy1_, qx2_, qy2_, pointList,
                rectangleFilterFlag);

        JsonNode jsonNode = mapper.readTree(response);
        this.linearTimeStr = jsonNode.get("query time").asText();

        // 同树查询一样，线性查询也会返回 rnd 并由 keyServer 提供 resultAddRnd
        // 拉取并复原结果，保存到 linearResultStr
        java.util.List<Info> rndInfoList = new java.util.ArrayList<>();
        JsonNode rndInfoListNode = jsonNode.get("query rnd");
        for (JsonNode rndInfoNode : rndInfoListNode) {
            BigInteger id = new BigInteger(rndInfoNode.get("id").asText());
            BigInteger x1 = new BigInteger(rndInfoNode.get("x1").asText());
            BigInteger y1 = new BigInteger(rndInfoNode.get("y1").asText());
            BigInteger x1_ = new BigInteger(rndInfoNode.get("x1_").asText());
            BigInteger y1_ = new BigInteger(rndInfoNode.get("y1_").asText());
            Info rndInfo = new Info(id, x1, y1, x1_, y1_);
            rndInfoList.add(rndInfo);
        }

        String resultAddRndStr = Sender.sendGetResultAddRnd();
        jsonNode = mapper.readTree(resultAddRndStr);
        java.util.List<Info> resultAddRndInfoList = new java.util.ArrayList<>();
        JsonNode resultAddRndInfoListNode = jsonNode.get("resultAddRnd");
        for (JsonNode resultAddRndInfoNode : resultAddRndInfoListNode) {
            BigInteger id = new BigInteger(resultAddRndInfoNode.get("id").asText());
            BigInteger x1 = new BigInteger(resultAddRndInfoNode.get("x1").asText());
            BigInteger y1 = new BigInteger(resultAddRndInfoNode.get("y1").asText());
            BigInteger x1_ = new BigInteger(resultAddRndInfoNode.get("x1_").asText());
            BigInteger y1_ = new BigInteger(resultAddRndInfoNode.get("y1_").asText());
            Info resultAddRndInfo = new Info(id, x1, y1, x1_, y1_);
            resultAddRndInfoList.add(resultAddRndInfo);
        }

        java.util.List<Info> result = new java.util.ArrayList<>();
        for (int i = 0; i < rndInfoList.size(); i++) {
            BigInteger id = resultAddRndInfoList.get(i).id.subtract(rndInfoList.get(i).id);
            BigInteger x1 = resultAddRndInfoList.get(i).x1.subtract(rndInfoList.get(i).x1);
            BigInteger y1 = resultAddRndInfoList.get(i).y1.subtract(rndInfoList.get(i).y1);
            BigInteger x1_ = resultAddRndInfoList.get(i).x1_.subtract(rndInfoList.get(i).x1_);
            BigInteger y1_ = resultAddRndInfoList.get(i).y1_.subtract(rndInfoList.get(i).y1_);
            Info info = new Info(id, x1, y1, x1_, y1_);
            result.add(info);
        }

        this.linearResultStr = mapper.writeValueAsString(result);
        return this.linearTimeStr;
    }

    public int getEdgeNum() {
        return this.edgeNum;
    }

    public String getRectangleFilterFlagStr() {
        return this.rectangleFilterFlagStr;
    }

    public String getTimeStr() {
        return this.timeStr;
    }

    public String getLinearTimeStr() {
        return this.linearTimeStr;
    }

    public String getLinearResultStr() {
        return this.linearResultStr;
    }

    public String getQueryResultStr() {
        return this.queryResultStr;
    }

    public String getRegionInfoJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode rect = mapper.createObjectNode();
        rect.put("左下角x(密文)", this.s_qx1.toString());
        rect.put("左下角y(密文)", this.s_qy1.toString());
        rect.put("右上角x(密文)", this.s_qx2.toString());
        rect.put("右上角y(密文)", this.s_qy2.toString());
        rect.put("左下角x的负值(密文)", this.s_qx1_.toString());
        rect.put("左下角y的负值(密文)", this.s_qy1_.toString());
        rect.put("右上角x的负值(密文)", this.s_qx2_.toString());
        rect.put("右上角y的负值(密文)", this.s_qy2_.toString());

        com.fasterxml.jackson.databind.node.ArrayNode arr = mapper.createArrayNode();
        for (org.location.utils.AnchorPoint.Pair<java.math.BigInteger, java.math.BigInteger, java.math.BigInteger, java.math.BigInteger> p : this.pointList) {
            ObjectNode o = mapper.createObjectNode();
            o.put("x(密文)", p.getX1().toString());
            o.put("y(密文)", p.getY1().toString());
            o.put("-x(密文)", p.getX1_().toString());
            o.put("-y(密文)", p.getY1_().toString());
            arr.add(o);
        }

        ObjectNode root = mapper.createObjectNode();
        root.put("多边形边数", this.edgeNum);
        root.set("最小包围矩形(密文)", rect);
        root.set("多边形密文锚点", arr);
        return mapper.writeValueAsString(root);
    }
}
