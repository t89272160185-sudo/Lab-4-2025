package functions.meta;

import functions.Function;
import java.util.Objects;

public class Scale implements Function {
    private static final double EPS = Math.ulp(1.0);

    private final Function function;
    private final double scaleX;
    private final double scaleY;
    private final double leftBorder;
    private final double rightBorder;

    public Scale(Function function, double scaleX, double scaleY) {
        if (Math.abs(scaleX) <= EPS) {
            throw new IllegalArgumentException("scaleX must be non-zero");
        }
        this.function = Objects.requireNonNull(function, "function");
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        double scaledLeft = function.getLeftDomainBorder() * scaleX;
        double scaledRight = function.getRightDomainBorder() * scaleX;
        leftBorder = Math.min(scaledLeft, scaledRight);
        rightBorder = Math.max(scaledLeft, scaledRight);
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
        double originalX = x / scaleX;
        double value = function.getFunctionValue(originalX);
        if (Double.isNaN(value)) {
            return Double.NaN;
        }
        return scaleY * value;
    }
}
