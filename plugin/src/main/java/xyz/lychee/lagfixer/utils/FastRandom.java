package xyz.lychee.lagfixer.utils;

import java.util.Arrays;
import java.util.Random;

/**
 * Implementation of George Marsaglia's elegant Xorshift random generator which is
 * 30% faster and better quality than the built-in java.util.random see also see
 * http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
 */
public class FastRandom extends Random implements Cloneable {
    private static final long serialVersionUID = 1L;

    protected long seed;

    public FastRandom() {
        this(System.nanoTime());
    }

    public FastRandom(long seed) {
        this.seed = seed;
    }

    public synchronized long getSeed() {
        return seed;
    }

    public synchronized void setSeed(long seed) {
        this.seed = seed;
        super.setSeed(seed);
    }

    public synchronized void setSeed(int[] array) {
        if (array.length == 0)
            // 汉化文本Array length must be greater than zero
            throw new IllegalArgumentException("数组长度必须大于 0");
        setSeed(Arrays.hashCode(array));
    }

    public FastRandom clone() throws CloneNotSupportedException {
        return (FastRandom) super.clone();
    }

    @Override
    protected int next(int nbits) {
        long x = seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        seed = x;
        x &= ((1L << nbits) - 1);

        return (int) x;
    }
}