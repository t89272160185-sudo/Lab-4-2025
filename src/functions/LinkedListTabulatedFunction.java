package functions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LinkedListTabulatedFunction implements TabulatedFunction, Externalizable {
    private static final long serialVersionUID = 1L;
    private static final double EPS = Math.ulp(1.0);

    private static class FunctionNode {
        FunctionPoint point;
        FunctionNode prev;
        FunctionNode next;
    }

    private final FunctionNode head = new FunctionNode();
    private int pointsCount;
    private FunctionNode cacheNode;
    private int cacheIndex;

    public LinkedListTabulatedFunction() {
        initEmptyList();
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        this();
        validateBorders(leftX, rightX, pointsCount);
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + step * i;
            FunctionNode node = addNodeToTail();
            node.point = new FunctionPoint(x, 0.0);
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        this(leftX, rightX, values.length);
        FunctionNode node = head.next;
        for (double value : values) {
            node.point.setY(value);
            node = node.next;
        }
    }

    public LinkedListTabulatedFunction(FunctionPoint[] sourcePoints) {
        this();
        if (sourcePoints.length < 2) {
            throw new IllegalArgumentException("At least two points are required");
        }
        FunctionPoint previous = null;
        for (FunctionPoint point : sourcePoints) {
            if (previous != null && !(point.getX() - previous.getX() > EPS)) {
                throw new IllegalArgumentException("Points must be strictly increasing by X");
            }
            FunctionNode node = addNodeToTail();
            node.point = point.clone();
            previous = point;
        }
    }

    private void initEmptyList() {
        head.next = head;
        head.prev = head;
        pointsCount = 0;
        cacheNode = null;
        cacheIndex = -1;
    }

    private void validateBorders(double leftX, double rightX, int count) {
        if (!(rightX - leftX > EPS)) {
            throw new IllegalArgumentException("left bound must be smaller than right bound");
        }
        if (count < 2) {
            throw new IllegalArgumentException("Function requires at least two points");
        }
    }

    private void dropCache() {
        cacheNode = null;
        cacheIndex = -1;
    }

    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Index: " + index);
        }
        FunctionNode current;
        int currentIndex;
        int distanceFromHead = index;
        int distanceFromTail = pointsCount - 1 - index;
        int cacheDistance = cacheNode == null ? Integer.MAX_VALUE : Math.abs(index - cacheIndex);

        if (cacheDistance <= distanceFromHead && cacheDistance <= distanceFromTail) {
            current = cacheNode;
            currentIndex = cacheIndex;
        } else if (distanceFromHead <= distanceFromTail) {
            current = head.next;
            currentIndex = 0;
        } else {
            current = head.prev;
            currentIndex = pointsCount - 1;
        }

        while (currentIndex < index) {
            current = current.next;
            currentIndex++;
        }
        while (currentIndex > index) {
            current = current.prev;
            currentIndex--;
        }

        cacheNode = current;
        cacheIndex = index;
        return current;
    }

    private FunctionNode addNodeToTail() {
        FunctionNode node = new FunctionNode();
        node.prev = head.prev;
        node.next = head;
        head.prev.next = node;
        head.prev = node;
        pointsCount++;
        dropCache();
        return node;
    }

    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Index: " + index);
        }
        if (index == pointsCount) {
            return addNodeToTail();
        }
        FunctionNode nextNode = getNodeByIndex(index);
        FunctionNode node = new FunctionNode();
        node.next = nextNode;
        node.prev = nextNode.prev;
        nextNode.prev.next = node;
        nextNode.prev = node;
        pointsCount++;
        dropCache();
        return node;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        FunctionNode node = getNodeByIndex(index);
        node.prev.next = node.next;
        node.next.prev = node.prev;
        pointsCount--;
        dropCache();
        return node;
    }

    private void ensureXFits(FunctionNode node, double x) throws InappropriateFunctionPointException {
        if (node.prev != head && !(x - node.prev.point.getX() > EPS)) {
            throw new InappropriateFunctionPointException("X overlaps previous point");
        }
        if (node.next != head && !(node.next.point.getX() - x > EPS)) {
            throw new InappropriateFunctionPointException("X overlaps next point");
        }
    }

    @Override
    public double getLeftDomainBorder() {
        return head.next.point.getX();
    }

    @Override
    public double getRightDomainBorder() {
        return head.prev.point.getX();
    }

    @Override
    public int getPointsCount() {
        return pointsCount;
    }

    @Override
    public FunctionPoint getPoint(int index) {
        return getNodeByIndex(index).point.clone();
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);
        ensureXFits(node, point.getX());
        node.point = point.clone();
    }

    @Override
    public double getPointX(int index) {
        return getNodeByIndex(index).point.getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);
        ensureXFits(node, x);
        node.point.setX(x);
    }

    @Override
    public double getPointY(int index) {
        return getNodeByIndex(index).point.getY();
    }

    @Override
    public void setPointY(int index, double y) {
        getNodeByIndex(index).point.setY(y);
    }

    @Override
    public void deletePoint(int index) {
        if (pointsCount < 3) {
            throw new IllegalStateException("Function must keep at least two points");
        }
        deleteNodeByIndex(index);
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        int index = 0;
        FunctionNode current = head.next;
        while (current != head && current.point.getX() < point.getX() - EPS) {
            current = current.next;
            index++;
        }
        if (current != head && Math.abs(current.point.getX() - point.getX()) <= EPS) {
            throw new InappropriateFunctionPointException("Point with same X already exists");
        }
        FunctionNode node = addNodeByIndex(index);
        node.point = point.clone();
    }

    @Override
    public double getFunctionValue(double x) {
        // В README не описано поведение вне [left; right], поэтому возвращаем NaN для таких аргументов.
        if (x < getLeftDomainBorder() - EPS || x > getRightDomainBorder() + EPS) {
            return Double.NaN;
        }
        if (Math.abs(x - getLeftDomainBorder()) <= EPS) {
            return head.next.point.getY();
        }
        if (Math.abs(x - getRightDomainBorder()) <= EPS) {
            return head.prev.point.getY();
        }
        FunctionNode node = head.next;
        while (node.next != head) {
            double x1 = node.point.getX();
            double x2 = node.next.point.getX();
            if (x >= x1 - EPS && x <= x2 + EPS) {
                double y1 = node.point.getY();
                double y2 = node.next.point.getY();
                double k = (x - x1) / (x2 - x1);
                return y1 + k * (y2 - y1);
            }
            node = node.next;
        }
        return Double.NaN;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(pointsCount);
        FunctionNode node = head.next;
        while (node != head) {
            out.writeDouble(node.point.getX());
            out.writeDouble(node.point.getY());
            node = node.next;
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        initEmptyList();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            FunctionNode node = addNodeToTail();
            node.point = new FunctionPoint(x, y);
        }
    }
}
