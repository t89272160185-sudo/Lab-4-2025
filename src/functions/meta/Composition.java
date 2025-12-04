package functions.meta;

import functions.Function;
import java.util.Objects;

public class Composition implements Function {
    private static final double EPS = Math.ulp(1.0);

    private final Function outer;
    private final Function inner;

    public Composition(Function outer, Function inner) {
        this.outer = Objects.requireNonNull(outer, "outer");
        this.inner = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public double getLeftDomainBorder() {
        return inner.getLeftDomainBorder();
    }

    @Override
    public double getRightDomainBorder() {
        return inner.getRightDomainBorder();
    }

    @Override
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS) {
            return Double.NaN;
        }
        double innerValue = inner.getFunctionValue(x);
        if (Double.isNaN(innerValue)) {
            return Double.NaN;
        }
        if (innerValue < outer.getLeftDomainBorder() - EPS || innerValue > outer.getRightDomainBorder() + EPS) {
            return Double.NaN;
        }
        return outer.getFunctionValue(innerValue);
    }
}
