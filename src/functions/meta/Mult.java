package functions.meta;

import functions.Function;
import java.util.Objects;

public class Mult implements Function {
    private static final double EPS = Math.ulp(1.0);

    private final Function first;
    private final Function second;
    private final double leftBorder;
    private final double rightBorder;

    public Mult(Function first, Function second) {
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");
        leftBorder = Math.max(first.getLeftDomainBorder(), second.getLeftDomainBorder());
        rightBorder = Math.min(first.getRightDomainBorder(), second.getRightDomainBorder());
        if (leftBorder - rightBorder > EPS) {
            throw new IllegalArgumentException("Functions do not intersect on X axis");
        }
    }

    @Override
    public double getLeftDomainBorder() {
        return leftBorder;
    }

    @Override
    public double getRightDomainBorder() {
        return rightBorder;
    }

    @Override
    public double getFunctionValue(double x) {
        if (x < leftBorder - EPS || x > rightBorder + EPS) {
            return Double.NaN;
        }
        return first.getFunctionValue(x) * second.getFunctionValue(x);
    }
}
