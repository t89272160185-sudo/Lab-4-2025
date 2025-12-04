package functions.meta;

import functions.Function;
import java.util.Objects;

public class Power implements Function {
    private final Function baseFunction;
    private final double power;

    public Power(Function baseFunction, double power) {
        this.baseFunction = Objects.requireNonNull(baseFunction, "baseFunction");
        this.power = power;
    }

    @Override
    public double getLeftDomainBorder() {
        return baseFunction.getLeftDomainBorder();
    }

    @Override
    public double getRightDomainBorder() {
        return baseFunction.getRightDomainBorder();
    }

    @Override
    public double getFunctionValue(double x) {
        double baseValue = baseFunction.getFunctionValue(x);
        if (Double.isNaN(baseValue)) {
            return Double.NaN;
        }
        return Math.pow(baseValue, power);
    }
}
