package org.location;

import java.util.ArrayList;
import java.util.List;

/**
 * 明文四叉树
 */
public class Quadtree {
    private int MINSIZE; // 叶子节点代表范围的边长
    private Node root; // 根节点

    /**
     * 树节点
     */
    private static class Node {
        int x1, y1, x2, y2;
        Node A = null, B = null, C = null, D = null;

        Node(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    /**
     * 四叉树中要存储的位置相关信息类
     */
    private static class Info {
        public int id;
        int x1, y1;

        Info(int id, int x1, int y1) {
            this.id = id;
            this.x1 = x1;
            this.y1 = y1;
        }
    }

    /**
     * 初始化明文四叉树
     * 
     * @param x1      (x1, y1)左下角点坐标
     * @param y1      (x1, y1)左下角点坐标
     * @param MINSIZE 最小范围边长
     * @param num     节点单轴所包含的最小边长的数量
     */
    Quadtree(int x1, int y1, int MINSIZE, int num) {
        this.MINSIZE = MINSIZE;
        int x2 = x1 + MINSIZE * num;
        int y2 = y1 + MINSIZE * num;
        root = new Node(x1, y1, x2, y2);
        InitHelper(root, num);
    }

    // 递归初始化节点
    private void InitHelper(Node node, int num) {
        if (num == 1) {
            return;
        }
        num = num / 2;
        int x1 = node.x1, y1 = node.y1; // left-bottom

        int x3 = node.x2, y3 = node.y2; // right-top

        int x2 = x1 + MINSIZE * num;
        int y2 = y1 + MINSIZE * num;

        Node A = new Node(x1, y2, x2, y3);
        node.A = A;
        Node B = new Node(x2, y2, x3, y3);
        node.B = B;
        Node C = new Node(x1, y1, x2, y2);
        node.C = C;
        Node D = new Node(x2, y1, x3, y2);
        node.D = D;
        InitHelper(A, num);
        InitHelper(B, num);
        InitHelper(C, num);
        InitHelper(D, num);
    }

    /**
     * 判断点是否在节点内
     * 
     * @param node 四叉树节点
     * @param a    点的x轴坐标值
     * @param b    点的y轴坐标值
     * @return 点是否在节点内
     */
    public boolean InArea(Node node, int a, int b) {
        if (a <= node.x2 && a >= node.x1 && b <= node.y2 && b >= node.y1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 四叉树中插入一个带位置点的信息,并返回插入的路径序号列表
     * 
     * @param id 信息id
     * @param x1 点的x轴值
     * @param y1 点的y轴值
     * @return 将点插入时的路径序号列表
     */
    public List<Integer> Insert(int id, int x1, int y1) {
        Info info = new Info(id, x1, y1);
        List<Integer> index = new ArrayList<>();
        if (!InsertHelper(root, info, index)) {
            System.out.println("Insert failed, insert data is not within the allowed range");
            return null;
        } else {
            // System.out.println("Inserted successfully");
            return index;
        }
    }

    /**
     * 插入辅助函数
     * 将信息插入四叉树，并将插入的路径序号存储在index中
     * 
     * @param node  四叉树节点
     * @param info  位置信息
     * @param index 路径序号列表
     * @return
     */
    private boolean InsertHelper(Node node, Info info, List<Integer> index) {
        if (!InArea(node, info.x1, info.y1))
            return false;

        if (node.A == null) {
            if (info.id >= 0) {
            }
            ;
            return true;
        } else {
            if (InArea(node.A, info.x1, info.y1)) {
                index.add(0);
                return InsertHelper(node.A, info, index);
            } else if (InArea(node.B, info.x1, info.y1)) {
                index.add(1);
                return InsertHelper(node.B, info, index);
            } else if (InArea(node.C, info.x1, info.y1)) {
                index.add(2);
                return InsertHelper(node.C, info, index);
            } else if (InArea(node.D, info.x1, info.y1)) {
                index.add(3);
                return InsertHelper(node.D, info, index);
            }
        }
        return false;
    }
}
