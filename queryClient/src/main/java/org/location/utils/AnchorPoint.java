package org.location.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.location.SHE;

import java.math.BigInteger;
import java.util.List;

public class AnchorPoint {
        /**
         * 位置坐标及其负值坐标
         *
         * @param <T1>
         * @param <T2>
         * @param <T3>
         * @param <T4>
         */
        public static class Pair<T1, T2, T3, T4> {
                @JsonSerialize(using = ToStringSerializer.class)
                T1 x1;
                @JsonSerialize(using = ToStringSerializer.class)
                T2 y1;
                @JsonSerialize(using = ToStringSerializer.class)
                T3 x1_;
                @JsonSerialize(using = ToStringSerializer.class)
                T4 y1_;

                public Pair(T1 x1, T2 y1, T3 x1_, T4 y1_) {
                        this.x1 = x1;
                        this.y1 = y1;
                        this.x1_ = x1_;
                        this.y1_ = y1_;
                }

                public T1 getX1() {
                        return x1;
                }

                public T2 getY1() {
                        return y1;
                }

                public T3 getX1_() {
                        return x1_;
                }

                public T4 getY1_() {
                        return y1_;
                }
        }

        public static void setPointList(SHE she, List<Pair<BigInteger, BigInteger, BigInteger, BigInteger>> pointList,
                        int edge_num) {
                if (edge_num == 6) {
                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4255500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4325500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4255500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4325500"))));

                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4255500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4326000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4255500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4326000"))));
                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4255000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4325000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4255000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4325000"))));
                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4255000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4325500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4255000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4325500"))));
                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4326000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4326000"))));

                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4325000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4325000"))));
                        pointList.add(new Pair<BigInteger, BigInteger, BigInteger, BigInteger>(
                                        she.SHEPublicKeyEncryption(new BigInteger("4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("4325500")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4256000")),
                                        she.SHEPublicKeyEncryption(new BigInteger("-4325500"))));
                }
        }
}
