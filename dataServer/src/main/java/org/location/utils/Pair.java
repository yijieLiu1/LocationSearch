package org.location.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 位置坐标及其负值坐标
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */
public class Pair<T1, T2, T3, T4> {
    @JsonSerialize(using = ToStringSerializer.class)
    public T1 x1;
    @JsonSerialize(using = ToStringSerializer.class)
    public T2 y1;
    @JsonSerialize(using = ToStringSerializer.class)
    public T3 x1_;
    @JsonSerialize(using = ToStringSerializer.class)
    public T4 y1_;

    public Pair() {
        // 默认构造函数
    }

    public Pair(T1 x1, T2 y1, T3 x1_, T4 y1_) {
        this.x1 = x1;
        this.y1 = y1;
        this.x1_ = x1_;
        this.y1_ = y1_;
    }
}
