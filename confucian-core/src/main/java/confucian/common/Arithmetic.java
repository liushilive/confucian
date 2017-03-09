package confucian.common;

import java.math.BigDecimal;

/**
 * double算术运算
 */
public interface Arithmetic {
    /**
     * The constant DEF_DIV_SCALE.
     */
    int DEF_DIV_SCALE = 2;

    /**
     * 加
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     *
     * @return the double
     */
    static double add(double v1, double v2, double... v3) {
        BigDecimal b = getBigDecimal(v1).add(getBigDecimal(v2));
        for (double v : v3) b = getBigDecimal(v).add(b);
        return b.doubleValue();
    }

    /**
     * 除
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     *
     * @return the double
     */
    static double div(double v1, double v2, double... v3) {
        double b = div(v1, v2, DEF_DIV_SCALE);
        for (double v : v3) b = div(b, v, DEF_DIV_SCALE);
        return b;
    }

    /**
     * 除，四舍五入
     *
     * @param v1    the v 1
     * @param v2    the v 2
     * @param scale the scale
     *
     * @return the double
     */
    static double div(double v1, double v2, int scale) {
        if (scale < 0) throw new IllegalArgumentException("小数位数必须是正整数或零");
        return getBigDecimal(v1).divide(getBigDecimal(v2), scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * Gets big decimal.
     *
     * @param value the value
     *
     * @return the big decimal
     */
    static BigDecimal getBigDecimal(double value) {
        return new BigDecimal(Double.toString(value));
    }

    /**
     * 乘
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     *
     * @return the double
     */
    static double mul(double v1, double v2, double... v3) {
        BigDecimal b;
        b = getBigDecimal(v1).multiply(getBigDecimal(v2));
        for (double v : v3) b = b.multiply(getBigDecimal(v));
        return b.doubleValue();
    }

    /**
     * 四舍五入
     *
     * @param v     the v
     * @param scale the scale
     *
     * @return the double
     */
    static double round(double v, int scale) {
        if (scale < 0) throw new IllegalArgumentException("小数位数必须是正整数或零");
        return getBigDecimal(v).divide(new BigDecimal("1"), scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 减
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @param v3 the v 3
     *
     * @return the double
     */
    static double sub(double v1, double v2, double... v3) {
        BigDecimal b = getBigDecimal(v1).subtract(getBigDecimal(v2));
        for (double v : v3) b = b.subtract(getBigDecimal(v));
        return b.doubleValue();
    }
}

