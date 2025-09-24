package org.location;

import org.location.utils.Info;
import org.location.utils.Sender;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {
    // 同态加密算法
    private SHE she;

    // 明文四叉树参数
    private Quadtree quadtree;
    private int lb_x;
    private int lb_y;
    private int treeDepth;
    private int treeHeight;
    private int edgeSize;

    private List<Info> allPlaintextPointInfo;
    private List<Info> allCiphertextPointInfo;
    private List<List<Integer>> allIndex;

    DataGenerator(int lb_x, int lb_y, int treeDepth, int edgeSize, SHE she) throws IOException {
        this.lb_x = lb_x;
        this.lb_y = lb_y;
        this.treeDepth = treeDepth;
        this.treeHeight = treeDepth + 1;
        this.edgeSize = edgeSize;
        this.she = she;

        // 2^7
        int block_num = 1 << treeDepth; // 要将根节点范围的一个轴划分为多少个区块
        // 每一块的长度，600*600
        int minsize = edgeSize / block_num; // 根据区块数量计算最小区块的边长

        // 计算四叉树根节点代表范围的矩形坐标及其加密坐标
        BigInteger x1 = new BigInteger(String.valueOf(lb_x));
        BigInteger y1 = new BigInteger(String.valueOf(lb_y));
        BigInteger x1_ = x1.multiply(new BigInteger("-1"));
        BigInteger y1_ = y1.multiply(new BigInteger("-1"));

        BigInteger sx1 = she.SHEPublicKeyEncryption(x1);
        BigInteger sy1 = she.SHEPublicKeyEncryption(y1);
        BigInteger sx1_ = she.SHEPublicKeyEncryption(x1_);
        BigInteger sy1_ = she.SHEPublicKeyEncryption(y1_);
        BigInteger MINSIZE = new BigInteger(String.valueOf(minsize));
        BigInteger num = new BigInteger(String.valueOf(block_num));
        BigInteger sx2_ = she.SHEPublicKeyEncryption(x1.add(MINSIZE.multiply(num)).multiply(new BigInteger("-1")));
        BigInteger sy2_ = she.SHEPublicKeyEncryption(y1.add(MINSIZE.multiply(num)).multiply(new BigInteger("-1")));
        MINSIZE = she.SHEPublicKeyEncryption(MINSIZE);

        // 初始化明文四叉树
        quadtree = new Quadtree(lb_x, lb_y, minsize, block_num);

        // 发送密文四叉树初始化参数
        Sender.sendQuadtreeParameter(sx1, sy1, sx1_, sy1_, sx2_, sy2_, MINSIZE, num);

        // 数据信息列表
        allPlaintextPointInfo = new ArrayList<>(2000);
        allCiphertextPointInfo = new ArrayList<>(2000);
        allIndex = new ArrayList<>(2000);
    }

//    public void generateLocations(int num) throws IOException {
//        // 随机生成数据点并插入明文四叉树
//        Random rnd = new Random();
//        rnd.setSeed(1);
//
//        // 定义目标区域
//        int minX = 4255000;
//        int maxX = 4256000;
//        int minY = 4325000;
//        int maxY = 4326000;
//        double ratio = 0.8; // 80% 的点落在目标区域
//
//        for (int i = 0; i < num; i++) {
//            int id = i;
//            BigInteger id_s = she.SHEPublicKeyEncryption(BigInteger.valueOf(i));
//
//            // 决定该点是否生成在小区域
//            int px1, py1;
//            if (rnd.nextDouble() < ratio) {
//                px1 = minX + rnd.nextInt(maxX - minX + 1);
//                py1 = minY + rnd.nextInt(maxY - minY + 1);
//            } else {
//                px1 = this.lb_x + rnd.nextInt(this.edgeSize);
//                py1 = this.lb_y + rnd.nextInt(this.edgeSize);
//            }
//
//            BigInteger rnd1 = BigInteger.valueOf(px1);
//            BigInteger rnd2 = BigInteger.valueOf(py1);
//            BigInteger px1_s = she.SHEPublicKeyEncryption(rnd1);
//            BigInteger py1_s = she.SHEPublicKeyEncryption(rnd2);
//            BigInteger px1_ = she.SHEPublicKeyEncryption(rnd1.negate());
//            BigInteger py1_ = she.SHEPublicKeyEncryption(rnd2.negate());
//
//            Info plaintextInfo = new Info(
//                    BigInteger.valueOf(id),
//                    BigInteger.valueOf(px1),
//                    BigInteger.valueOf(py1),
//                    BigInteger.valueOf(-px1),
//                    BigInteger.valueOf(-py1));
//            Info ciphertextInfo = new Info(id_s, px1_s, py1_s, px1_, py1_);
//
//            // 添加到明文/密文列表
//            allPlaintextPointInfo.add(plaintextInfo);
//            allCiphertextPointInfo.add(ciphertextInfo);
//
//            // 插入明文四叉树
//            List<Integer> index = quadtree.Insert(id, px1, py1);
//            allIndex.add(index);
//        }
//
//        // ############################################################################################
//        // 插入一个固定可查询点
//        int id = 999999999;
//        int px1 = 4255500;
//        int py1 = 4325500;
//        BigInteger id_s = she.SHEPublicKeyEncryption(BigInteger.valueOf(id));
//        BigInteger rnd1 = BigInteger.valueOf(px1);
//        BigInteger rnd2 = BigInteger.valueOf(py1);
//        BigInteger px1_s = she.SHEPublicKeyEncryption(rnd1);
//        BigInteger py1_s = she.SHEPublicKeyEncryption(rnd2);
//        BigInteger px1_ = she.SHEPublicKeyEncryption(rnd1.negate());
//        BigInteger py1_ = she.SHEPublicKeyEncryption(rnd2.negate());
//
//        Info plaintextInfo = new Info(
//                BigInteger.valueOf(id),
//                BigInteger.valueOf(px1),
//                BigInteger.valueOf(py1),
//                BigInteger.valueOf(-px1),
//                BigInteger.valueOf(-py1));
//        Info ciphertextInfo = new Info(id_s, px1_s, py1_s, px1_, py1_);
//
//        allPlaintextPointInfo.add(plaintextInfo);
//        allCiphertextPointInfo.add(ciphertextInfo);
//        List<Integer> index = quadtree.Insert(id, px1, py1);
//        allIndex.add(index);
//        // ###########################################################################################
//    }

     public void generateLocations(int num) throws IOException {
     // 随机生成数据点并插入明文四叉树
     Random rnd = new Random();
     rnd.setSeed(1);
     for (int i = 0; i < num; i++) {
     int id = i;
     BigInteger id_s = she.SHEPublicKeyEncryption(new
     BigInteger(Integer.toString(i)));
     int px1 = this.lb_x + rnd.nextInt(this.edgeSize);
     int py1 = this.lb_y + rnd.nextInt(this.edgeSize);

     BigInteger rnd1 = new BigInteger(Integer.toString(px1));
     BigInteger rnd2 = new BigInteger(Integer.toString(py1));
     BigInteger px1_s = she.SHEPublicKeyEncryption(rnd1);
     BigInteger py1_s = she.SHEPublicKeyEncryption(rnd2);
     BigInteger px1_ = she.SHEPublicKeyEncryption(rnd1.multiply(new
     BigInteger("-1")));
     BigInteger py1_ = she.SHEPublicKeyEncryption(rnd2.multiply(new
     BigInteger("-1")));

     Info plaintextInfo = new Info(
     new BigInteger(String.valueOf(id)),
     new BigInteger(String.valueOf(px1)),
     new BigInteger(String.valueOf(py1)),
     new BigInteger(String.valueOf(-px1)),
     new BigInteger(String.valueOf(-py1)));
     Info ciphertextInfo = new Info(id_s, px1_s, py1_s, px1_, py1_);

     // 将随机生成的明文位置数据信息添加到全部点信息的列表
     allPlaintextPointInfo.add(plaintextInfo);
     // 将随机生成的密文位置数据信息添加到全部点信息的列表
     allCiphertextPointInfo.add(ciphertextInfo);

     // 在明文四叉树中找到位置点插入的路径序号列表
     List<Integer> index = quadtree.Insert(id, px1, py1);
     allIndex.add(index);
     }

     //
//     ############################################################################################
     // 插入一个可以被查询到的点
     int id = 999999999;
     BigInteger id_s = she.SHEPublicKeyEncryption(new
     BigInteger(Integer.toString(id)));
     int px1 = 4255500;
     int py1 = 4325500;

     BigInteger rnd1 = new BigInteger(Integer.toString(px1));
     BigInteger rnd2 = new BigInteger(Integer.toString(py1));
     BigInteger px1_s = she.SHEPublicKeyEncryption(rnd1);
     BigInteger py1_s = she.SHEPublicKeyEncryption(rnd2);
     BigInteger px1_ = she.SHEPublicKeyEncryption(rnd1.multiply(new
     BigInteger("-1")));
     BigInteger py1_ = she.SHEPublicKeyEncryption(rnd2.multiply(new
     BigInteger("-1")));

     Info plaintextInfo = new Info(
     new BigInteger(String.valueOf(id)),
     new BigInteger(String.valueOf(px1)),
     new BigInteger(String.valueOf(py1)),
     new BigInteger(String.valueOf(-px1)),
     new BigInteger(String.valueOf(-py1)));
     Info ciphertextInfo = new Info(id_s, px1_s, py1_s, px1_, py1_);

     // 将随机生成的明文位置数据信息添加到全部点信息的列表
     allPlaintextPointInfo.add(plaintextInfo);
     // 将随机生成的密文位置数据信息添加到全部点信息的列表
     allCiphertextPointInfo.add(ciphertextInfo);

     // 在明文四叉树中找到位置点插入的路径序号列表
     List<Integer> index = quadtree.Insert(id, px1, py1);
     allIndex.add(index);
     //
//     ###########################################################################################
     }

    public void sendLocationData() throws IOException {
        for (int i = 0; i < this.allCiphertextPointInfo.size(); i++) {
            // 发送待插入位置信息
            Sender.sendLocationData(allIndex.get(i), allCiphertextPointInfo.get(i));
        }
    }

    public int getTreeHeight() {
        return this.treeHeight;
    }

    public int getLocationNum() {
        return this.allPlaintextPointInfo.size();
    }

    public List<Info> getPlaintextPointInfo() {
        return this.allPlaintextPointInfo;
    }

    public List<Info> getCiphertextPointInfo() {
        return this.allCiphertextPointInfo;
    }
}
