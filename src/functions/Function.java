package functions;

/**
 * Base abstraction of a single-variable function.
 */
public interface Function {
    double getLeftDomainBorder();

    double getRightDomainBorder();

    double getFunctionValue(double x);
}
