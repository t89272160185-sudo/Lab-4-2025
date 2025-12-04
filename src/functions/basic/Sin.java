package functions.basic;

public class Sin extends TrigonometricFunction {
    @Override
    protected double evaluate(double x) {
        return Math.sin(x);
    }
}
