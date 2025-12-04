package functions.meta;

import functions.Function;
import java.util.Objects;

public class Shift implements Function {
    private static final double EPS = Math.ulp(1.0);

    private final Function function;
    private final double shiftX;
    private final double shiftY;

    public Shift(Function function, double shiftX, double shiftY) {
        this.function = Objects.requireNonNull(function, "function");
        this.shiftX = shiftX;
        this.shiftY = shiftY;
    }

    @Override
    public double getLeftDomainBorder() {
        return function.getLeftDomainBorder() + shiftX;
    }

    @Override
    public double getRightDomainBorder() {
        return function.getRightDomainBorder() + shiftX;
    }

    @Override
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS) {
            return Double.NaN;
        }
        double originalValue = function.getFunctionValue(x - shiftX);
        if (Double.isNaN(originalValue)) {
            return Double.NaN;
        }
        return originalValue + shiftY;
    }
}
