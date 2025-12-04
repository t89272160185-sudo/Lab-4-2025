package functions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.Writer;

public final class TabulatedFunctions {
    private static final double EPS = Math.ulp(1.0);

    private TabulatedFunctions() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static TabulatedFunction tabulate(Function function, double leftX, double rightX, int pointsCount) {
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        if (!(rightX - leftX > EPS)) {
            throw new IllegalArgumentException("leftX must be less than rightX");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("At least two points are required");
        }
        if (leftX < function.getLeftDomainBorder() - EPS || rightX > function.getRightDomainBorder() + EPS) {
            throw new IllegalArgumentException("Segment is outside function domain");
        }

        FunctionPoint[] points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            double y = function.getFunctionValue(x);
            if (Double.isNaN(y)) {
                throw new IllegalArgumentException("Function value is undefined inside domain at x=" + x);
            }
            points[i] = new FunctionPoint(x, y);
        }
        return new ArrayTabulatedFunction(points);
    }

    public static void outputTabulatedFunction(TabulatedFunction function, OutputStream out) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
        dataOut.writeInt(function.getPointsCount());
        for (int i = 0; i < function.getPointsCount(); i++) {
            FunctionPoint point = function.getPoint(i);
            dataOut.writeDouble(point.getX());
            dataOut.writeDouble(point.getY());
        }
        dataOut.flush();
    }

    public static TabulatedFunction inputTabulatedFunction(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(new BufferedInputStream(in));
        int pointsCount = dataIn.readInt();
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            double x = dataIn.readDouble();
            double y = dataIn.readDouble();
            points[i] = new FunctionPoint(x, y);
        }
        return new ArrayTabulatedFunction(points);
    }

    public static void writeTabulatedFunction(TabulatedFunction function, Writer out) throws IOException {
        BufferedWriter writer = new BufferedWriter(out);
        StringBuilder builder = new StringBuilder();
        builder.append(function.getPointsCount());
        for (int i = 0; i < function.getPointsCount(); i++) {
            FunctionPoint point = function.getPoint(i);
            builder.append(' ')
                   .append(point.getX())
                   .append(' ')
                   .append(point.getY());
        }
        writer.write(builder.toString());
        writer.newLine();
        writer.flush();
    }

    public static TabulatedFunction readTabulatedFunction(Reader in) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new BufferedReader(in));
        int pointsCount = (int) nextNumber(tokenizer);
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; i++) {
            double x = nextNumber(tokenizer);
            double y = nextNumber(tokenizer);
            points[i] = new FunctionPoint(x, y);
        }
        return new ArrayTabulatedFunction(points);
    }

    private static double nextNumber(StreamTokenizer tokenizer) throws IOException {
        int token = tokenizer.nextToken();
        if (token != StreamTokenizer.TT_NUMBER) {
            throw new IOException("Expected number token");
        }
        return tokenizer.nval;
    }
}
