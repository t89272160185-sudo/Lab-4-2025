package functions;

import java.io.Serializable;

/**
  Immutable pair of function arguments; setters mutate the stored coordinates directly.
 */
public class FunctionPoint implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    public FunctionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public FunctionPoint(FunctionPoint source) {
        this(source.x, source.y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public FunctionPoint clone() {
        try {
            return (FunctionPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone should be supported", e);
        }
    }

    @Override
    public String toString() {
        return "(" + x + "; " + y + ")";
    }
}
