package org.location.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigInteger;

/**
 * 单个用户的信息内容，存储包括单条数据的id和位置数据信息，如果需要有额外的信息可以在这个类中进行扩展
 */
public class Info {
    @JsonSerialize(using = ToStringSerializer.class)
    BigInteger id; // 数据id
    @JsonSerialize(using = ToStringSerializer.class)
    BigInteger x1, y1, x1_, y1_; // 位置坐标，及其坐标的负数值

    /**
     * 存储信息条目内容的初始化
     *
     * @param id  数据id
     * @param x1  位置的x轴值
     * @param y1  位置的y轴值
     * @param x1_ 位置的x轴值的负数值
     * @param y1_ 位置的y轴值的负数值
     */
    public Info(BigInteger id, BigInteger x1, BigInteger y1,
         BigInteger x1_, BigInteger y1_) {
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x1_ = x1_;
        this.y1_ = y1_;
    }

    /**
     * 判断两条信息是否相同
     *
     * @param info 需要对比的另一条信息
     * @return 信息是否相同
     */
    public boolean equals(Info info) {
        return id.equals(info.id) && x1.equals(info.x1) && y1.equals(info.y1);
    }

    @Override
    public String toString() {
        return "Info{" +
                "id=" + id +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", x1_=" + x1_ +
                ", y1_=" + y1_ +
                '}';
    }
}
