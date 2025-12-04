package functions;

import java.io.Serializable;

public class ArrayTabulatedFunction implements TabulatedFunction, Serializable {
    private static final long serialVersionUID = 1L;
    private static final double EPS = Math.ulp(1.0);

    private FunctionPoint[] points;
    private int pointsCount;

    public ArrayTabulatedFunction(double leftX, double rightX, int pointsCount) {
        validateBorders(leftX, rightX, pointsCount);
        this.points = new FunctionPoint[pointsCount];
        this.pointsCount = pointsCount;
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + step * i;
            points[i] = new FunctionPoint(x, 0.0);
        }
    }

    public ArrayTabulatedFunction(double leftX, double rightX, double[] values) {
        this(leftX, rightX, values.length);
        for (int i = 0; i < values.length; i++) {
            points[i].setY(values[i]);
        }
    }

    public ArrayTabulatedFunction(FunctionPoint[] sourcePoints) {
        if (sourcePoints.length < 2) {
            throw new IllegalArgumentException("At least two points are required");
        }
        points = new FunctionPoint[sourcePoints.length];
        pointsCount = sourcePoints.length;
        for (int i = 0; i < sourcePoints.length; i++) {
            points[i] = sourcePoints[i].clone();
        }
        ensureSortedAndUnique();
    }

    private void validateBorders(double leftX, double rightX, int count) {
        if (!(rightX - leftX > EPS)) {
            throw new IllegalArgumentException("left bound must be smaller than right bound");
        }
        if (count < 2) {
            throw new IllegalArgumentException("Function requires at least two points");
        }
    }

    private void ensureSortedAndUnique() {
        for (int i = 1; i < pointsCount; i++) {
            if (!(points[i].getX() - points[i - 1].getX() > EPS)) {
                throw new IllegalArgumentException("Points must be strictly increasing by X");
            }
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Index: " + index);
        }
    }

    private void ensureXFits(int index, double x) throws InappropriateFunctionPointException {
        if (index > 0 && !(x - points[index - 1].getX() > EPS)) {
            throw new InappropriateFunctionPointException("X overlaps previous point");
        }
        if (index < pointsCount - 1 && !(points[index + 1].getX() - x > EPS)) {
            throw new InappropriateFunctionPointException("X overlaps next point");
        }
    }

    @Override
    public double getLeftDomainBorder() {
        return points[0].getX();
    }

    @Override
    public double getRightDomainBorder() {
        return points[pointsCount - 1].getX();
    }

    @Override
    public int getPointsCount() {
        return pointsCount;
    }

    @Override
    public FunctionPoint getPoint(int index) {
        checkIndex(index);
        return points[index].clone();
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        checkIndex(index);
        ensureXFits(index, point.getX());
        points[index] = point.clone();
    }

    @Override
    public double getPointX(int index) {
        checkIndex(index);
        return points[index].getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        checkIndex(index);
        ensureXFits(index, x);
        points[index].setX(x);
    }

    @Override
    public double getPointY(int index) {
        checkIndex(index);
        return points[index].getY();
    }

    @Override
    public void setPointY(int index, double y) {
        checkIndex(index);
        points[index].setY(y);
    }

    @Override
    public void deletePoint(int index) {
        checkIndex(index);
        if (pointsCount < 3) {
            throw new IllegalStateException("Function must keep at least two points");
        }
        for (int i = index; i < pointsCount - 1; i++) {
            points[i] = points[i + 1];
        }
        points[pointsCount - 1] = null;
        pointsCount--;
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        int insertIndex = findInsertIndex(point.getX());
        if (insertIndex < pointsCount && Math.abs(points[insertIndex].getX() - point.getX()) <= EPS) {
            throw new InappropriateFunctionPointException("Point with same X already exists");
        }
        ensureCapacity(pointsCount + 1);
        for (int i = pointsCount; i > insertIndex; i--) {
            points[i] = points[i - 1];
        }
        points[insertIndex] = point.clone();
        pointsCount++;
    }

    @SuppressWarnings("ManualArrayToCollectionCopy")
    private void ensureCapacity(int desired) {
        if (points.length >= desired) {
            return;
        }
        FunctionPoint[] newArray = new FunctionPoint[desired];
        for (int i = 0; i < pointsCount; i++) {
            newArray[i] = points[i];
        }
        points = newArray;
    }

    private int findInsertIndex(double x) {
        int low = 0;
        int high = pointsCount - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midX = points[mid].getX();
            if (Math.abs(midX - x) <= EPS) {
                return mid;
            } else if (midX < x) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    @Override
    public double getFunctionValue(double x) {
        // В задании поведение вне области определения не определено, поэтому возвращаем NaN.
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS) {
            return Double.NaN;
        }
        if (Math.abs(x - getLeftDomainBorder()) <= EPS) {
            return points[0].getY();
        }
        if (Math.abs(x - getRightDomainBorder()) <= EPS) {
            return points[pointsCount - 1].getY();
        }
        for (int i = 0; i < pointsCount - 1; i++) {
            double x1 = points[i].getX();
            double x2 = points[i + 1].getX();
            if (x >= x1 - EPS && x <= x2 + EPS) {
                double y1 = points[i].getY();
                double y2 = points[i + 1].getY();
                double k = (x - x1) / (x2 - x1);
                return y1 + k * (y2 - y1);
            }
        }
        return Double.NaN;
    }
}
