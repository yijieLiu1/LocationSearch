package org.location;

import org.location.utils.Info;
import org.location.utils.Pair;
import org.location.utils.Sender;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EncryptedQuadtree {
    private SHE she;
    private BigInteger MINSIZE;
    private Node root;

    /**
     * 四叉树节点
     */
    private static class Node {
        // 节点的代表的正方形坐标，(x1,y1)为左下角坐标，(x2,y2)为右上角坐标
        BigInteger x1, y1, x2, y2;
        // 对应坐标值的负数值，用于加速方案计算速度
        BigInteger x1_, y1_, x2_, y2_;
        // 节点的四个子节点
        Node A = null, B = null, C = null, D = null;
        // 节点存储的信息列表，如果为叶子节点，则会存储用户位置信息等
        List<Info> infoList;

        /**
         * 初始化四叉树节点
         *
         * @param x1  正方形左下角点的x轴坐标值
         * @param y1  正方形左下角点的y轴坐标值
         * @param x2  正方形右上角点的x轴坐标值
         * @param y2  正方形右上角点的y轴坐标值
         * @param x1_ 方形左下角点的x轴坐标值对应的负数值
         * @param y1_ 正方形左下角点的y轴坐标值对应的负数值
         * @param x2_ 正方形右上角点的x轴坐标值对应的负数值
         * @param y2_ 正方形右上角点的y轴坐标值对应的负数值
         */
        Node(BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2, BigInteger x1_, BigInteger y1_, BigInteger x2_,
                BigInteger y2_) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x1_ = x1_;
            this.y1_ = y1_;
            this.x2_ = x2_;
            this.y2_ = y2_;
        }
    }

    /*
     * Create data search tree and initialize
     * -(x1, y1) are the coordinates of the lower left corner (encrypted)
     * -MINSIZE is the minimum region width size (encrypted)
     * -num is the minimum number of regions that each edge of the region can hold,
     * i.e. the total minimum number of regions is num*num (plaintext)
     */

    EncryptedQuadtree(SHE she) {
        this.she = she; // 同态加密算法
    }

    /**
     * 初始化同态算法参数和创建数据搜索四叉树
     *
     * @param x1      (x1, y1)为四叉树节点左下角点坐标(传入为加密后坐标值)
     * @param y1      (x1, y1)为四叉树节点左下角点坐标(传入为加密后坐标值)
     * @param x1_     (x1_, y1_)为四叉树节点左下角点坐标负值(传入为加密后坐标值负值)
     * @param y1_     (x1_, y1_)为四叉树节点左下角点坐标负值(传入为加密后坐标值负值)
     * @param x2_     (x2_, y2_)为四叉树节点右上角点坐标负值(传入为加密后坐标值负值)
     * @param y2_     (x2_, y2_)为四叉树节点右上角点坐标负值(传入为加密后坐标值负值)
     * @param MINSIZE 四叉树最小节点的正方形范围边长
     * @param num     根节点区域要划分的单边正方形数量，即每个轴要分割成几个正方形
     */
    public void Init(BigInteger x1, BigInteger y1, BigInteger x1_, BigInteger y1_, BigInteger x2_, BigInteger y2_,
            BigInteger MINSIZE, BigInteger num) {
        this.MINSIZE = MINSIZE; // 四叉树中叶子节点正方形的边长，即最小节点的边长
        // 计算根节点的右上角加密后坐标点
        BigInteger x2 = x1.add(MINSIZE.multiply(num));
        BigInteger y2 = y1.add(MINSIZE.multiply(num));
        root = new Node(x1, y1, x2, y2, x1_, y1_, x2_, y2_);
        InitHelper(root, num); // 辅助构造加密四叉树
    }

    /**
     * 四叉树构建辅助函数
     * 四叉树子节点布局为：
     * | A | B |
     * | C | D |
     *
     * @param node 四叉树节点
     * @param num  单边要继续划分的正方形数量
     */
    private void InitHelper(Node node, BigInteger num) {
        // num=1意味着已经是四叉树叶子节点，可以存储相关数据信息了
        if (num.equals(new BigInteger("1"))) {
            node.infoList = new ArrayList<>();
            return;
        }

        // 加密数据状态下计算各子节点坐标值
        num = num.divide(new BigInteger("2"));
        BigInteger x1 = node.x1, y1 = node.y1; // left-bottom
        BigInteger x1_ = node.x1_, y1_ = node.y1_;

        BigInteger x3 = node.x2, y3 = node.y2; // right-top
        BigInteger x3_ = node.x2_, y3_ = node.y2_;

        BigInteger x2 = x1.add(MINSIZE.multiply(num));
        BigInteger y2 = y1.add(MINSIZE.multiply(num));
        BigInteger x2_ = x3_.add(MINSIZE.multiply(num));
        BigInteger y2_ = y3_.add(MINSIZE.multiply(num));

        Node A = new Node(x1, y2, x2, y3, x1_, y2_, x2_, y3_);
        node.A = A;
        Node B = new Node(x2, y2, x3, y3, x2_, y2_, x3_, y3_);
        node.B = B;
        Node C = new Node(x1, y1, x2, y2, x1_, y1_, x2_, y2_);
        node.C = C;
        Node D = new Node(x2, y1, x3, y2, x2_, y1_, x3_, y2_);
        node.D = D;
        InitHelper(A, num);
        InitHelper(B, num);
        InitHelper(C, num);
        InitHelper(D, num);
    }

    /*
     * Determine if the point is in the region, return True if it is in the region,
     * otherwise return False
     */

    /**
     * 加密状态下判断一个点是否在四叉树节点范围内
     *
     * @param node 四叉树节点
     * @param a    点的x轴坐标值
     * @param b    点的y轴坐标值
     * @param a_   点的x轴坐标值的负值
     * @param b_   点的y轴坐标值的负值
     * @return 位置点是否在节点范围内
     */
    public boolean InArea(Node node, BigInteger a, BigInteger b, BigInteger a_, BigInteger b_) throws Exception {
        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1

        /*
         * 安全乘法计算，详见论文密文乘法协议，原理相同，和论文描述实施有差异，实际上这里是可以封装成一个函数然后方便后续调用的
         * 为什么这里要额外构造一个密文乘法协议，因为直接使用同态加密的密文乘法同态特性存在乘法深度的限制问题，这样可能会存在多次计算后报错的问题
         * 而重新构建的乘法协议因为存在一个解密的协作过程，使得密文乘法深度不会存在问题
         *
         * 在真实部署时，其实这里是一个两方协作的过程，但是本代码只侧重方案，未做工程实践的区分。
         * 在工程实践中应该由数据的存储方对数据加扰，然后发送给持有私钥的协作方，协作方解密，然后进行系列运算，告诉数据存储方resX_decode的比较结果
         */
        // BigInteger k1_x = she.SHEDecryption(a.add(node.x1_.add(r0)));
        // BigInteger k2_x = she.SHEDecryption(node.x2.add(a_.add(r1)));
        // BigInteger resX =
        // she.SHEPublicKeyEncryption(k1_x.multiply(k2_x)).add(r1.multiply(node.x1.add(a_))).add(r0.multiply(a.add(node.x2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resX = Sender.sendForMultiply(a.add(node.x1_.add(r0)), node.x2.add(a_.add(r1)))
                .add(r1.multiply(node.x1.add(a_)))
                .add(r0.multiply(a.add(node.x2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // 安全乘法计算，详见论文，原理相同，和论文描述实施有差异
        // BigInteger k1_y = she.SHEDecryption(b.add(node.y1_.add(r0)));
        // BigInteger k2_y = she.SHEDecryption(node.y2.add(b_.add(r1)));
        // BigInteger resY =
        // she.SHEPublicKeyEncryption(k1_y.multiply(k2_y)).add(r1.multiply(node.y1.add(b_))).add(r0.multiply(b.add(node.y2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resY = Sender.sendForMultiply(b.add(node.y1_.add(r0)), node.y2.add(b_.add(r1)))
                .add(r1.multiply(node.y1.add(b_)))
                .add(r0.multiply(b.add(node.y2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // 给加密数据加扰动后再解密，避免拥有私钥的一方直接解密出原始的值
        // BigInteger resX_decode = she.SHEDecryption(r0.multiply(resX).add(r1));
        // BigInteger resY_decode = she.SHEDecryption(r0.multiply(resY).add(r1));
        BigInteger resX_decode = Sender.sendForComparison(r0.multiply(resX).add(r1));
        BigInteger resY_decode = Sender.sendForComparison(r0.multiply(resY).add(r1));

        /*
         * 通过解密后值的比较判断点是否在矩形范围内，原理很简单，见论文Secure Rectangle Intersection Decision
         * Protocol部分
         * 判断点是否在矩形内和判断两个矩形是否相交用的一个原理
         */
        if (resX_decode.compareTo(new BigInteger("0")) > 0 && resY_decode.compareTo(new BigInteger("0")) > 0)
            return true;
        else
            return false;
    }

    /**
     * 给定矩形坐标数据和位置点数据，判断位置点是否在矩形内
     *
     * @param x1  (x1, y1)为矩形左下角点
     * @param y1  (x1, y1)为矩形左下角点
     * @param x2  (x2, y2)为矩形右上角点
     * @param y2  (x2, y2)为矩形右上角点
     * @param x1_ (x1_, y1_)为矩形左下角点负值
     * @param y1_ (x1_, y1_)为矩形左下角点负值
     * @param x2_ (x2_, y2_)为矩形右上角点负值
     * @param y2_ (x2_, y2_)为矩形右上角点负值
     * @param a   位置点的x轴坐标值
     * @param b   位置点的y轴坐标值
     * @param a_  位置点的x轴坐标值负值
     * @param b_  位置点的y轴坐标值负值
     * @return 点是否在矩形内
     */
    public boolean InArea(BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2, BigInteger x1_, BigInteger y1_,
            BigInteger x2_, BigInteger y2_, BigInteger a, BigInteger b, BigInteger a_, BigInteger b_) throws Exception {
        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1

        // BigInteger k1_x = she.SHEDecryption(a.add(x1_.add(r0)));
        // BigInteger k2_x = she.SHEDecryption(x2.add(a_.add(r1)));
        // BigInteger resX =
        // she.SHEPublicKeyEncryption(k1_x.multiply(k2_x)).add(r1.multiply(x1.add(a_))).add(r0.multiply(a.add(x2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resX = Sender.sendForMultiply(a.add(x1_.add(r0)), x2.add(a_.add(r1)))
                .add(r1.multiply(x1.add(a_)))
                .add(r0.multiply(a.add(x2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // BigInteger k1_y = she.SHEDecryption(b.add(y1_.add(r0)));
        // BigInteger k2_y = she.SHEDecryption(y2.add(b_.add(r1)));
        // BigInteger resY =
        // she.SHEPublicKeyEncryption(k1_y.multiply(k2_y)).add(r1.multiply(y1.add(b_))).add(r0.multiply(b.add(y2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resY = Sender.sendForMultiply(b.add(y1_.add(r0)), y2.add(b_.add(r1)))
                .add(r1.multiply(y1.add(b_)))
                .add(r0.multiply(b.add(y2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // BigInteger resX_decode = she.SHEDecryption(r0.multiply(resX).add(r1));
        // BigInteger resY_decode = she.SHEDecryption(r0.multiply(resY).add(r1));
        BigInteger resX_decode = Sender.sendForComparison(r0.multiply(resX).add(r1));
        BigInteger resY_decode = Sender.sendForComparison(r0.multiply(resY).add(r1));

        if (resX_decode.compareTo(new BigInteger("0")) > 0 && resY_decode.compareTo(new BigInteger("0")) > 0)
            return true;
        else
            return false;
    }

    /**
     * 判断给定矩形和四叉树节点区域是否存在重合
     * <p>
     * 所用到的原理和前面判断点是否在矩形内是一样的，同样代码未区分协作实体，在工程化时需要注意区分，一个实体拥有加解密功能
     * 另一个实体存储了四叉树等加密数据，并没有解密功能。数据存储的实体发送给解密实体时的数据都要加扰，不然数据就有泄露风险
     *
     * @param node 四叉树节点
     * @param x1   (x1, y1)为矩形左下角点
     * @param y1   (x1, y1)为矩形左下角点
     * @param x2   (x2, y2)为矩形右上角点
     * @param y2   (x2, y2)为矩形右上角点
     * @param x1_  (x1_, y1_)为矩形左下角点负值
     * @param y1_  (x1_, y1_)为矩形左下角点负值
     * @param x2_  (x2_, y2_)为矩形右上角点负值
     * @param y2_  (x2_, y2_)为矩形右上角点负值
     * @return 两个矩形是否重合
     */
    private boolean AreaOverlap(Node node, BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2, BigInteger x1_,
            BigInteger y1_, BigInteger x2_, BigInteger y2_) throws Exception {
        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1

        // BigInteger k1_x = she.SHEDecryption(x2.add(node.x1_.add(r0)));
        // BigInteger k2_x = she.SHEDecryption(node.x2.add(x1_.add(r1)));
        // BigInteger resX =
        // she.SHEPublicKeyEncryption(k1_x.multiply(k2_x)).add(r1.multiply(node.x1.add(x2_))).add(r0.multiply(x1.add(node.x2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resX = Sender.sendForMultiply(x2.add(node.x1_.add(r0)), node.x2.add(x1_.add(r1)))
                .add(r1.multiply(node.x1.add(x2_)))
                .add(r0.multiply(x1.add(node.x2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // BigInteger k1_y = she.SHEDecryption(y2.add(node.y1_.add(r0)));
        // BigInteger k2_y = she.SHEDecryption(node.y2.add(y1_.add(r1)));
        // BigInteger resY =
        // she.SHEPublicKeyEncryption(k1_y.multiply(k2_y)).add(r1.multiply(node.y1.add(y2_))).add(r0.multiply(y1.add(node.y2_))).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        BigInteger resY = Sender.sendForMultiply(y2.add(node.y1_.add(r0)), node.y2.add(y1_.add(r1)))
                .add(r1.multiply(node.y1.add(y2_)))
                .add(r0.multiply(y1.add(node.y2_)))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));

        // BigInteger resX_decode = she.SHEDecryption(r0.multiply(resX).add(r1));
        // BigInteger resY_decode = she.SHEDecryption(r0.multiply(resY).add(r1));
        BigInteger resX_decode = Sender.sendForComparison(r0.multiply(resX).add(r1));
        BigInteger resY_decode = Sender.sendForComparison(r0.multiply(resY).add(r1));

        if (resX_decode.compareTo(new BigInteger("0")) > 0 && resY_decode.compareTo(new BigInteger("0")) > 0)
            return true;
        else
            return false;
    }

    /**
     * 判断四叉树节点是否在给定矩形内部
     *
     * @param node 四叉树节点
     * @param x1   (x1, y1)为矩形左下角点
     * @param y1   (x1, y1)为矩形左下角点
     * @param x2   (x2, y2)为矩形右上角点
     * @param y2   (x2, y2)为矩形右上角点
     * @param x1_  (x1_, y1_)为矩形左下角点负值
     * @param y1_  (x1_, y1_)为矩形左下角点负值
     * @param x2_  (x2_, y2_)为矩形右上角点负值
     * @param y2_  (x2_, y2_)为矩形右上角点负值
     * @return 四叉树节点是否在给定矩形内部
     */
    private boolean InRectangle(Node node, BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2, BigInteger x1_,
            BigInteger y1_, BigInteger x2_, BigInteger y2_) throws Exception {

        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1

        // BigInteger k1_x = she.SHEDecryption(r0.multiply(node.x1.add(x1_)).add(r1));
        // BigInteger k2_x = she.SHEDecryption(r0.multiply(x2.add(node.x2_)).add(r1));
        BigInteger k1_x = Sender.sendForComparison(r0.multiply(node.x1.add(x1_)).add(r1));
        BigInteger k2_x = Sender.sendForComparison(r0.multiply(x2.add(node.x2_)).add(r1));

        // BigInteger k1_y = she.SHEDecryption(r0.multiply(node.y1.add(y1_)).add(r1));
        // BigInteger k2_y = she.SHEDecryption(r0.multiply(y2.add(node.y2_)).add(r1));
        BigInteger k1_y = Sender.sendForComparison(r0.multiply(node.y1.add(y1_)).add(r1));
        BigInteger k2_y = Sender.sendForComparison(r0.multiply(y2.add(node.y2_)).add(r1));

        if (k1_x.compareTo(new BigInteger("0")) > 0 && k2_x.compareTo(new BigInteger("0")) > 0
                && k1_y.compareTo(new BigInteger("0")) > 0 && k2_y.compareTo(new BigInteger("0")) > 0)
            return true;
        else
            return false;
    }

    /**
     * 根据在明文情况下构建的四叉树得到的位置点要插入的节点位置，来将位置数据插入到对应的密文节点
     * 此操作是因为在论文方案中，为了加速方案速度，初始数据的构造是由数据收集所有者完成的，这个实体拥有收集位置数据的能力，
     * 同时可以自行构造四叉树，先将收集到的位置点对应的四叉树节点确定好，再插入到密文节点中存储。这样避免了在密文环境中插入数据，
     * 可以有效提升方案速度。
     * <p>
     * 但是在我们6G项目的工程实践中，实际上是需要在密文四叉树中插入数据的，本代码并未实现这个功能，但参考明文插入的方式实现即可，
     * 本代码提供了密文下判断一个点是否在一个节点内函数，因此密文下的位置点数据插入也是可以轻松实现的
     *
     * @param info  待插入的信息
     * @param index 位置点相关信息在明文四叉树中插入的路径，路径为一系列0-3的数字，代表要插入哪一个子节点
     */
    public void Insert(Info info, List<Integer> index) {
        if (!InsertHelper(root, info, index))
            System.out.println("Insert failed, insert data is not within the allowed range");
        else
            // System.out.println("Inserted successfully");
            return;
    }

    /* insert helper function */
    private boolean InsertHelper(Node node, Info info, List<Integer> index) {
        // 找到对应叶子节点，信息直接存储在此子节点信息列表中
        if (index.isEmpty()) {
            node.infoList.add(info);
            return true;
        } else {
            // 找到要插入的子节点序号，插入到对应子节点，直至找到叶子节点
            int flag = index.remove(0);
            if (flag == 0) {
                return InsertHelper(node.A, info, index);
            } else if (flag == 1) {
                return InsertHelper(node.B, info, index);
            } else if (flag == 2) {
                return InsertHelper(node.C, info, index);
            } else if (flag == 3) {
                return InsertHelper(node.D, info, index);
            }
        }
        return false;
    }

    /* delete */
    public void Delete(Info info) throws Exception {
        if (!InArea(root, info.x1, info.y1, info.x1_, info.y1_)) {
            System.out.println("The data that needs to be deleted is not within the legal scope");
            return;
        }
        DeleteHelper(root, info);
        System.out.println("successfully deleted");
    }

    /* delete helper function */
    private void DeleteHelper(Node node, Info info) throws Exception {
        // 先判断区域是否存在重合
        if (!InArea(node, info.x1, info.y1, info.x1_, info.y1_))
            return;

        // 如果是叶子节点说明数据就在这里存储，删除数据
        if (node.A == null) {
            Iterator<Info> iterator = node.infoList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(info)) {
                    iterator.remove();
                }
            }
        } else {
            // 否则继续找到叶子节点进行数据删除
            DeleteHelper(node.A, info);
            DeleteHelper(node.B, info);
            DeleteHelper(node.C, info);
            DeleteHelper(node.D, info);
        }
    }

    /*
     * Query the contents of the request range
     * pointList is a list of anchor points,
     * the first point in the default list is the anchor point inside the polygon
     */

    /**
     * 查询请求函数
     *
     * @param x1                  (x1, y1)为矩形左下角点，矩形为包含凸多边形的最小矩形
     * @param y1                  (x1, y1)为矩形左下角点
     * @param x2                  (x2, y2)为矩形右上角点
     * @param y2                  (x2, y2)为矩形右上角点
     * @param x1_                 (x1_, y1_)为矩形左下角点负值
     * @param y1_                 (x1_, y1_)为矩形左下角点负值
     * @param x2_                 (x2_, y2_)为矩形右上角点负值
     * @param y2_                 (x2_, y2_)为矩形右上角点负值
     * @param pointList           凸多边形对应的锚点列表，默认第一个值为凸多变形内的锚点
     * @param rectangleFilterFlag 是否开启矩阵过滤
     *                            如果开启意味着搜索到叶子节点时，先判断点是否在矩阵内，再去判断点是否在多变形里
     *                            开启，则只要叶子节点和矩阵重合，则叶子节点里的位置全部加入待筛选列表
     * @param isLessAnchor        锚点数量是否需要翻倍，此参数仅用于论文性能测试，无实际工程用途，可以不理会
     * @return 给定范围内的位置点信息列表
     */
    public List<Info> Query(BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2, BigInteger x1_, BigInteger y1_,
            BigInteger x2_, BigInteger y2_, List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList,
            boolean rectangleFilterFlag, boolean isLessAnchor) throws Exception {
        List<Info> queryInfo = new ArrayList<>(1000);
        double stime = System.currentTimeMillis();
        // 初步位置点筛选，利用查询范围的矩形信息初步找到存在重合区域的所有四叉树叶子节点的位置信息列表
        QueryHelperForLargeRange(root, x1, y1, x2, y2, x1_, y1_, x2_, y2_, queryInfo, rectangleFilterFlag);
        double etime = System.currentTimeMillis();
        if (rectangleFilterFlag) {
            System.out.println("Tree filter and rectangle filter time(ms): " + String.valueOf(etime - stime));
        } else {
            System.out.println("Tree filter time(ms): " + String.valueOf(etime - stime));
        }

        // 利用凸多边形进行精准的筛选
        if (isLessAnchor) {
            // less point
            return QueryHelperForSmallRange(queryInfo, pointList);
        } else {
            // more point
            return QueryHelperForSmallRangeDouble(queryInfo, pointList);
        }
    }

    /**
     * 找到节点范围内和矩形重合和叶子节点，并将这些叶子节点存储的位置信息加入待筛选的查询列表
     *
     * @param node                四叉树节点
     * @param x1                  (x1, y1)为矩形左下角点，矩形为包含凸多边形的最小矩形
     * @param y1                  (x1, y1)为矩形左下角点
     * @param x2                  (x2, y2)为矩形右上角点
     * @param y2                  (x2, y2)为矩形右上角点
     * @param x1_                 (x1_, y1_)为矩形左下角点负值
     * @param y1_                 (x1_, y1_)为矩形左下角点负值
     * @param x2_                 (x2_, y2_)为矩形右上角点负值
     * @param y2_                 (x2_, y2_)为矩形右上角点负值
     * @param queryInfo           待筛选的位置点信息列表
     * @param rectangleFilterFlag 是否开启矩形过滤
     */
    private void QueryHelperForLargeRange(Node node, BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2,
            BigInteger x1_, BigInteger y1_, BigInteger x2_, BigInteger y2_, List<Info> queryInfo,
            boolean rectangleFilterFlag) throws Exception {
        // 无重合区域直接跳过
        if (!AreaOverlap(node, x1, y1, x2, y2, x1_, y1_, x2_, y2_))
            return;

        // 为叶子节点时判断加入哪些位置信息
        if (node.A == null) {
            if (rectangleFilterFlag) {
                if (InRectangle(node, x1, y1, x2, y2, x1_, y1_, x2_, y2_)) {
                    queryInfo.addAll(node.infoList);
                } else {
                    for (Info info : node.infoList) {
                        if (InArea(x1, y1, x2, y2, x1_, y1_, x2_, y2_, info.x1, info.y1, info.x1_, info.y1_)) {
                            queryInfo.add(info);
                        }
                    }
                }
            } else {
                queryInfo.addAll(node.infoList);
            }
        } else {
            // 非叶子节点时，继续递归到叶子节点
            QueryHelperForLargeRange(node.A, x1, y1, x2, y2, x1_, y1_, x2_, y2_, queryInfo, rectangleFilterFlag);
            QueryHelperForLargeRange(node.B, x1, y1, x2, y2, x1_, y1_, x2_, y2_, queryInfo, rectangleFilterFlag);
            QueryHelperForLargeRange(node.C, x1, y1, x2, y2, x1_, y1_, x2_, y2_, queryInfo, rectangleFilterFlag);
            QueryHelperForLargeRange(node.D, x1, y1, x2, y2, x1_, y1_, x2_, y2_, queryInfo, rectangleFilterFlag);
        }
    }

    /**
     * 返回待筛选列表中真正在凸多边形内的位置点的信息列表
     *
     * @param queryInfo 矩形初筛的位置点列表
     * @param pointList 凸多边形的锚点列表，注意默认第一个锚点为凸多边形内的锚点
     * @return 最终的查询到的位置点数据
     */
    public List<Info> QueryHelperForSmallRange(List<Info> queryInfo,
            List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList) throws Exception {
        List<Info> resultInfo = new ArrayList<>(500);
        double stime = System.currentTimeMillis();
        Iterator<Info> iterator = queryInfo.iterator();
        while (iterator.hasNext()) {
            Info cur = iterator.next();

            // 先计算要判断的位置点与凸多边形内的锚点的欧式距离
            BigInteger x_dif = cur.x1.add(pointList.get(0).x1_);
            BigInteger x_dif_ = cur.x1_.add(pointList.get(0).x1);
            BigInteger y_dif = cur.y1.add(pointList.get(0).y1_);
            BigInteger y_dif_ = cur.y1_.add(pointList.get(0).y1);

            BigInteger stdDis = multiply(x_dif, x_dif, x_dif_, x_dif_).add(multiply(y_dif, y_dif, y_dif_, y_dif_));
            BigInteger stdDis_ = multiply(x_dif, x_dif_, x_dif_, x_dif).add(multiply(y_dif, y_dif_, y_dif_, y_dif));

            boolean needRemove = false;
            for (int i = 1; i < pointList.size(); i++) {

                // 计算要判断的位置点与凸多边形内的锚点关于各边对称锚点的欧式距离
                x_dif = cur.x1.add(pointList.get(i).x1_);
                x_dif_ = cur.x1_.add(pointList.get(i).x1);
                y_dif = cur.y1.add(pointList.get(i).y1_);
                y_dif_ = cur.y1_.add(pointList.get(i).y1);

                BigInteger dis = multiply(x_dif, x_dif, x_dif_, x_dif_).add(multiply(y_dif, y_dif, y_dif_, y_dif_));
                BigInteger dis_ = multiply(x_dif, x_dif_, x_dif_, x_dif).add(multiply(y_dif, y_dif_, y_dif_, y_dif));

                // 判断两个欧式距离的大小来判断位置点是否在凸多边形内
                if (!QueryHelperForDis(stdDis, dis, stdDis_, dis_)) {
                    needRemove = true;
                    break;
                }
            }
            if (!needRemove) {
                resultInfo.add(cur);
            }
        }
        double etime = System.currentTimeMillis();
        System.out.println("Change in the number of points: " + String.valueOf(queryInfo.size()) + " ---> "
                + String.valueOf(resultInfo.size()));
        System.out.println("Polygon filter time(ms): " + String.valueOf(etime - stime));
        return resultInfo;
    }

    /**
     * 仅仅用于论文锚点数量翻倍的性能测试，工程实践无需考虑
     *
     * @param queryInfo
     * @param pointList
     * @return
     */
    public List<Info> QueryHelperForSmallRangeDouble(List<Info> queryInfo,
            List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList) throws Exception {
        List<Info> resultInfo = new ArrayList<>(500);
        double stime = System.currentTimeMillis();
        Iterator<Info> iterator = queryInfo.iterator();
        while (iterator.hasNext()) {
            Info cur = iterator.next();
            // BigInteger stdDis = cur.x1.add(pointList.get(0).x1.multiply(E_1)).pow(2)
            // .add(cur.y1.add(pointList.get(0).y1.multiply(E_1)).pow(2));

            boolean needRemove = false;
            for (int i = 1; i < pointList.size(); i++) {
                BigInteger x_dif = cur.x1.add(pointList.get(0).x1_);
                BigInteger x_dif_ = cur.x1_.add(pointList.get(0).x1);
                BigInteger y_dif = cur.y1.add(pointList.get(0).y1_);
                BigInteger y_dif_ = cur.y1_.add(pointList.get(0).y1);

                BigInteger stdDis = multiply(x_dif, x_dif, x_dif_, x_dif_).add(multiply(y_dif, y_dif, y_dif_, y_dif_));
                BigInteger stdDis_ = multiply(x_dif, x_dif_, x_dif_, x_dif).add(multiply(y_dif, y_dif_, y_dif_, y_dif));

                x_dif = cur.x1.add(pointList.get(i).x1_);
                x_dif_ = cur.x1_.add(pointList.get(i).x1);
                y_dif = cur.y1.add(pointList.get(i).y1_);
                y_dif_ = cur.y1_.add(pointList.get(i).y1);

                BigInteger dis = multiply(x_dif, x_dif, x_dif_, x_dif_).add(multiply(y_dif, y_dif, y_dif_, y_dif_));
                BigInteger dis_ = multiply(x_dif, x_dif_, x_dif_, x_dif).add(multiply(y_dif, y_dif_, y_dif_, y_dif));

                if (!QueryHelperForDis(stdDis, dis, stdDis_, dis_)) {
                    needRemove = true;
                    break;
                }
            }
            if (!needRemove) {
                resultInfo.add(cur);
            }
        }
        double etime = System.currentTimeMillis();
        System.out.println("Change in the number of points: " + String.valueOf(queryInfo.size()) + " ---> "
                + String.valueOf(resultInfo.size()));
        System.out.println("Polygon filter time(ms): " + String.valueOf(etime - stime));
        return resultInfo;
    }

    /**
     * 比较stdDis和dis的大小
     *
     * @param stdDis  距离值stdDis
     * @param dis     距离值dis
     * @param stdDis_ 距离值stdDis对应的加密负值
     * @param dis_    距离值dis对应的加密负值
     * @return stdDis < dis时返回True
     */
    private boolean QueryHelperForDis(BigInteger stdDis, BigInteger dis, BigInteger stdDis_, BigInteger dis_)
            throws Exception {
        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1
        // BigInteger temp = she.SHEDecryption(r0.multiply(dis.add(stdDis_)).add(r1));
        BigInteger temp = Sender.sendForComparison(r0.multiply(dis.add(stdDis_)).add(r1));

        if (temp.compareTo(new BigInteger("0")) >= 0)
            return true;
        else
            return false;
    }

    /**
     * 安全加密乘法协议实现
     *
     * @param a  加密值a
     * @param b  加密值b
     * @param a_ 加密值a对应的负数加密值
     * @param b_ 加密值a对应的负数加密值
     * @return
     */
    public BigInteger multiply(BigInteger a, BigInteger b, BigInteger a_, BigInteger b_) throws Exception {
        Random rnd = new Random();
        BigInteger r1 = (new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1"));
        BigInteger r0 = r1.add(new BigInteger(she.k1 / 4, rnd)).add(new BigInteger("1")); // r0 > r1

        // BigInteger k1 = she.SHEDecryption(a.add(r0));
        // BigInteger k2 = she.SHEDecryption(b.add(r1));
        // return
        // she.SHEPublicKeyEncryption(k1.multiply(k2)).add(r1.multiply(a_)).add(r0.multiply(b_)).add(r0.multiply(r1).multiply(new
        // BigInteger("-1")));
        return Sender.sendForMultiply(a.add(r0), b.add(r1))
                .add(r1.multiply(a_)).add(r0.multiply(b_))
                .add(r0.multiply(r1).multiply(new BigInteger("-1")));
    }

    public static void main(String[] args) {
        System.out.println("Good ");
    }

    /**
     * 线性遍历扫描所有叶子节点中的位置点，进行矩形与多边形过滤。
     * 不使用四叉树的重合判断与剪枝，仅用于性能对比。
     */
    public List<Info> LinearQuery(BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2,
            BigInteger x1_, BigInteger y1_, BigInteger x2_, BigInteger y2_,
            List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList,
            boolean rectangleFilterFlag, boolean isLessAnchor) throws Exception {
        List<Info> all = new ArrayList<>(1000);
        collectAllInfos(this.root, all);

        List<Info> candidate;
        if (rectangleFilterFlag) {
            candidate = new ArrayList<>(all.size());
            for (Info info : all) {
                if (InArea(x1, y1, x2, y2, x1_, y1_, x2_, y2_, info.x1, info.y1, info.x1_, info.y1_)) {
                    candidate.add(info);
                }
            }
        } else {
            candidate = all;
        }

        if (isLessAnchor) {
            return QueryHelperForSmallRange(candidate, pointList);
        } else {
            return QueryHelperForSmallRangeDouble(candidate, pointList);
        }
    }

    /**
     * 收集整棵树的所有位置点（只读）。
     */
    private void collectAllInfos(Node node, List<Info> out) {
        if (node == null)
            return;
        if (node.A == null) {
            if (node.infoList != null && !node.infoList.isEmpty()) {
                out.addAll(node.infoList);
            }
            return;
        }
        collectAllInfos(node.A, out);
        collectAllInfos(node.B, out);
        collectAllInfos(node.C, out);
        collectAllInfos(node.D, out);
    }

    /**
     * 导出密文四叉树的结构摘要（只读）。
     * - 仅返回每个节点的坐标密文的字符串表示（可截断）
     * - 叶子节点仅返回包含的元素数量（leafSize），不导出具体密文点
     * - 通过 maxDepth 控制遍历深度，避免超大 JSON
     *
     * @param maxDepth       最大遍历深度（根为第1层）
     * @param maxLeafItems   预留参数，当前不导出叶子内容，固定忽略
     * @param truncateBigInt 是否对大整数进行字符串截断
     * @return 可序列化的树摘要对象
     */
    public Object exportTreeInfo(int maxDepth, int maxLeafItems, boolean truncateBigInt) {
        java.util.function.Function<java.math.BigInteger, String> fmt = v -> {
            String s = v.toString();
            if (!truncateBigInt)
                return s;
            return s.length() > 16 ? s.substring(0, 16) + "..." : s;
        };

        java.util.Map<String, Object> rootMap = new java.util.HashMap<>();
        rootMap.put("MINSIZE", fmt.apply(this.MINSIZE));

        java.util.function.BiFunction<Node, Integer, java.util.Map<String, Object>> snap = new java.util.function.BiFunction<Node, Integer, java.util.Map<String, Object>>() {
            @Override
            public java.util.Map<String, Object> apply(Node node, Integer depth) {
                if (node == null)
                    return null;
                java.util.Map<String, Object> view = new java.util.HashMap<>();
                view.put("x1", fmt.apply(node.x1));
                view.put("y1", fmt.apply(node.y1));
                view.put("x2", fmt.apply(node.x2));
                view.put("y2", fmt.apply(node.y2));
                if (node.A == null) {
                    int leafSize = (node.infoList == null) ? 0 : node.infoList.size();
                    view.put("leafSize", leafSize);
                    return view;
                }
                if (depth < maxDepth) {
                    view.put("A", apply(node.A, depth + 1));
                    view.put("B", apply(node.B, depth + 1));
                    view.put("C", apply(node.C, depth + 1));
                    view.put("D", apply(node.D, depth + 1));
                }
                return view;
            }
        };
        rootMap.put("tree", snap.apply(this.root, 1));
        return rootMap;
    }
}
