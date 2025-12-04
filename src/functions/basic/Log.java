package functions.basic;

import functions.Function;

public class Log implements Function {
    private static final double EPS = Math.ulp(1.0);
    private final double base;

    public Log(double base) {
        if (!(base > 0.0) || Math.abs(base - 1.0) <= EPS) {
            throw new IllegalArgumentException("Log base must be positive and not equal to 1");
        }
        this.base = base;
    }

    @Override
    public double getLeftDomainBorder() {
        return Double.MIN_VALUE;
    }

    @Override
    public double getRightDomainBorder() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getFunctionValue(double x) {
        if (!(x > 0)) {
            return Double.NaN;
        }
        return Math.log(x) / Math.log(base);
    }
}
