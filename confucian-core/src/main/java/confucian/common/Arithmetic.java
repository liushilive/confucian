package confucian.common;

import java.math.BigDecimal;

/**
 * double算术运算
 */
public class Arithmetic {
    private static final int DEF_DIV_SCALE = 2;

    private Arithmetic() {
    }

    /**
     * 加
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     * @return the double
     */
    public static double add(double v1, double v2, double... v3) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        BigDecimal b;
        b = b1.add(b2);
        for (double v : v3) {
            BigDecimal x = new BigDecimal(Double.toString(v));
            b = b.add(x);
        }
        return b.doubleValue();
    }

    /**
     * 除
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     * @return the double
     */
    public static double div(double v1, double v2, double... v3) {
        double b = div(v1, v2, DEF_DIV_SCALE);
        for (double v : v3) {
            b = div(b, v, DEF_DIV_SCALE);
        }
        return b;
    }

    /**
     * 除，四舍五入
     *
     * @param v1    the v 1
     * @param v2    the v 2
     * @param scale the scale
     * @return the double
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("小数位数必须是正整数或零");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 乘
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     * @return the double
     */
    public static double mul(double v1, double v2, double... v3) {
        BigDecimal b;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        b = b1.multiply(b2);
        for (double v : v3) {
            BigDecimal x = new BigDecimal(Double.toString(v));
            b = b.multiply(x);
        }
        return b.doubleValue();
    }

    /**
     * 四舍五入
     *
     * @param v     the v
     * @param scale the scale
     * @return the double
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("小数位数必须是正整数或零");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 减
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     * @return the double
     */
    public static double sub(double v1, double v2, double... v3) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        BigDecimal b;
        b = b1.subtract(b2);
        for (double v : v3) {
            BigDecimal x = new BigDecimal(Double.toString(v));
            b = b.subtract(x);
        }
        return b.doubleValue();
    }
}

