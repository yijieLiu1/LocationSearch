package org.location;

import java.math.BigInteger;
import java.util.Random;

/**
 * SHE加密算法
 */
public class SHE implements java.io.Serializable{
    public int k0; // p的比特长度
    public int k1; // 明文的最大比特长度，即明文的比特空间范围
    public int k2; // l的比特长度
    public int k3; // 公钥加密时随机数的比特长度

    private BigInteger p; // 大质数，比特长度k0
    private BigInteger q; // 大质数
    private BigInteger l;

    public BigInteger N; // N = p * q
    public BigInteger E0_1; // 公钥参数(E0_1, E0_2)，即0值加密后的两个不同的加密值
    public BigInteger E0_2; // 公钥参数(E0_1, E0_2)，即0值加密后的两个不同的加密值

    public SHE() {

    }

    /**
     * 算法参数初始化
     * @param k0 p的比特长度，即论文中的kp值
     * @param k1 明文空间的最大比特长度，即明文最大值的比特长度，即论文中的k0值
     * @param k2 l的比特长度，以及一些中途需要生成的随机数的比特长度，即论文中的k2值
     * @param k3 公钥进行加密时所需要的随机数的比特长度，即为论文中的k1值
     * @param num q的比特长度是p的比特长度的倍数，num=2，意味着q的比特长度大约为p的比特长度的2倍，num * k0为论文中的kq值
     */
    public SHE(int k0, int k1, int k2, int k3, int num)
    {
        Random rnd = new Random();
        this.p = BigInteger.probablePrime(k0, rnd);
        this.q = new BigInteger("1");
        BigInteger ans = BigInteger.probablePrime(k0, rnd);
        for (int i=0; i < num; i++) {
            this.q = this.q.multiply(ans);
        }
        this.l = BigInteger.probablePrime(k2, rnd);
        this.N = this.p.multiply(this.q);
        this.k0 = k0;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;

        // 生成两个公钥参数
        this.E0_1 = this.SHEEncryption(new BigInteger("0"));
        this.E0_2 = this.SHEEncryption(new BigInteger("0"));
    }

    public void Init(int k0, int k1, int k2, int k3, BigInteger p, BigInteger q, BigInteger l, BigInteger N, BigInteger E0_1, BigInteger E0_2) {
        this.k0 = k0;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.p = p;
        this.q = q;
        this.l = l;
        this.N = N;
        this.E0_1 = E0_1;
        this.E0_2 = E0_2;
    }

    /**
     * 加密
     * @param plaintext 明文值
     * @return 明文加密后的密文值
     */
    public BigInteger SHEEncryption(BigInteger plaintext)
    {
        Random rnd = new Random();

        BigInteger r0 = new BigInteger(this.k2, rnd);
        BigInteger r1 = new BigInteger(this.q.bitLength(), rnd);

        BigInteger tmp1 = ((r0.multiply(this.l).mod(this.N)).add(plaintext)).mod(this.N);
        BigInteger tmp2 = ((r1.multiply(this.p).mod(this.N)).add(BigInteger.ONE)).mod(this.N);

        return (tmp1.multiply(tmp2)).mod(this.N);
    }

    /**
     * 利用公钥进行的加密
     * @param plaintext 明文值
     * @return 明文加密后的密文值
     */
    public BigInteger SHEPublicKeyEncryption(BigInteger plaintext)
    {
        Random rnd = new Random();

        BigInteger r1 = new BigInteger(this.k3, rnd);
        BigInteger r2 = new BigInteger(this.k3, rnd);

        BigInteger tmp1 = r1.multiply(this.E0_1).mod(this.N);
        BigInteger tmp2 = r2.multiply(this.E0_2).mod(this.N);

        return plaintext.add(tmp1).add(tmp2).mod(this.N);
    }

    /**
     * 解密
     * @param ciphertext 密文值
     * @return 密文解密后的明文值
     */
    public BigInteger SHEDecryption(BigInteger ciphertext)
    {

        BigInteger plaintext = (ciphertext.mod(this.p)).mod(this.l);

        // 判断明文真实的正负性
        if(plaintext.compareTo(l.divide(new BigInteger("2"))) >= 0){
            plaintext = plaintext.subtract(l);
        }
        return plaintext;
    }

    public static void main(String[] args)
    {
        /* 算法测试 */

        /*
         * 初始化同态加密算法参数
         * 1024为p的比特长度
         * 40为明文空间的比特长度范围
         * 450为l的比特长度及一些随机数的比特长度
         * 80为公钥加密时所用到的随机数的比特长度
         * 33代表q的比特长度为p的约33倍
         */
        SHE she = new SHE(1024, 40, 450, 80, 33);
        long stime = System.currentTimeMillis();
        for (int i=0; i < 10000; i++) {
//            BigInteger ciphertext = she.SHEPublicKeyEncryption(new BigInteger(String.valueOf(i)));
            BigInteger ciphertext = she.SHEEncryption(new BigInteger(String.valueOf(i)));
            BigInteger plaintext = she.SHEDecryption(ciphertext);
        }
        long etime = System.currentTimeMillis();
        System.out.println("query time(ms): "+(etime - stime));

        BigInteger ciphertext = she.SHEPublicKeyEncryption(new BigInteger(String.valueOf(688)));
        BigInteger plaintext = she.SHEDecryption(ciphertext);
        System.out.println(plaintext);
    }
}