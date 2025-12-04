import functions.Function;
import functions.FunctionPoint;
import functions.Functions;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.TabulatedFunctions;
import functions.basic.Cos;
import functions.basic.Exp;
import functions.basic.Log;
import functions.basic.Sin;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final double STEP = 0.1;

    public static void main(String[] args) throws Exception {
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);

        Function sin = new Sin();
        Function cos = new Cos();

        System.out.println("=== Analytic sin(x) ===");
        printFunctionValues(sin, 0.0, Math.PI, STEP);
        System.out.println("=== Analytic cos(x) ===");
        printFunctionValues(cos, 0.0, Math.PI, STEP);

        TabulatedFunction tabulatedSin = TabulatedFunctions.tabulate(sin, 0.0, Math.PI, 10);
        TabulatedFunction tabulatedCos = TabulatedFunctions.tabulate(cos, 0.0, Math.PI, 10);
        System.out.println("=== sin(x): analytic vs tabulated (10 points) ===");
        compareFunctions(sin, tabulatedSin, 0.0, Math.PI, STEP);
        System.out.println("=== cos(x): analytic vs tabulated (10 points) ===");
        compareFunctions(cos, tabulatedCos, 0.0, Math.PI, STEP);

        Function sumSquares10 = Functions.sum(
                Functions.power(tabulatedSin, 2.0),
                Functions.power(tabulatedCos, 2.0)
        );
        System.out.println("=== sin^2(x) + cos^2(x) based on 10-point tabulation ===");
        printFunctionValues(sumSquares10, 0.0, Math.PI, STEP);

        TabulatedFunction tabulatedSinDense = TabulatedFunctions.tabulate(sin, 0.0, Math.PI, 25);
        TabulatedFunction tabulatedCosDense = TabulatedFunctions.tabulate(cos, 0.0, Math.PI, 25);
        Function sumSquares25 = Functions.sum(
                Functions.power(tabulatedSinDense, 2.0),
                Functions.power(tabulatedCosDense, 2.0)
        );
        System.out.println("=== sin^2(x) + cos^2(x) based on 25-point tabulation ===");
        printFunctionValues(sumSquares25, 0.0, Math.PI, STEP);

        Function exp = new Exp();
        TabulatedFunction tabulatedExp = TabulatedFunctions.tabulate(exp, 0.0, 10.0, 11);
        Path expTextPath = dataDir.resolve("exp_tabulated.txt");
        try (Writer writer = Files.newBufferedWriter(expTextPath)) {
            TabulatedFunctions.writeTabulatedFunction(tabulatedExp, writer);
        }
        TabulatedFunction expFromText;
        try (Reader reader = Files.newBufferedReader(expTextPath)) {
            expFromText = TabulatedFunctions.readTabulatedFunction(reader);
        }
        System.out.println("=== exp(x) text I/O verification ===");
        compareAtIntegerPoints(tabulatedExp, expFromText, 0.0);

        Function naturalLog = new Log(Math.E);
        double logLeftBorder = Math.nextUp(0.0); // ln(x) не определена при x=0, используем ближайшее положительное значение.
        TabulatedFunction tabulatedLog = TabulatedFunctions.tabulate(naturalLog, logLeftBorder, 10.0, 11);
        Path logBinaryPath = dataDir.resolve("ln_tabulated.bin");
        try (OutputStream out = Files.newOutputStream(logBinaryPath)) {
            TabulatedFunctions.outputTabulatedFunction(tabulatedLog, out);
        }
        TabulatedFunction logFromBinary;
        try (InputStream in = Files.newInputStream(logBinaryPath)) {
            logFromBinary = TabulatedFunctions.inputTabulatedFunction(in);
        }
        System.out.println("=== ln(x) binary I/O verification (x=0 заменён на минимальный double) ===");
        compareAtIntegerPoints(tabulatedLog, logFromBinary, logLeftBorder);

        Function lnOfExp = Functions.composition(naturalLog, new Exp());
        TabulatedFunction tabulatedLnExp = TabulatedFunctions.tabulate(lnOfExp, 0.0, 10.0, 11);
        Path serialPath = dataDir.resolve("ln_exp_serial.bin");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(serialPath))) {
            out.writeObject(tabulatedLnExp);
        }
        TabulatedFunction deserializedLnExp;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(serialPath))) {
            deserializedLnExp = (TabulatedFunction) in.readObject();
        }
        System.out.println("=== ln(exp(x)) Serializable verification ===");
        compareAtIntegerPoints(tabulatedLnExp, deserializedLnExp, 0.0);

        LinkedListTabulatedFunction listLnExp = new LinkedListTabulatedFunction(extractPoints(tabulatedLnExp));
        Path externalPath = dataDir.resolve("ln_exp_external.bin");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(externalPath))) {
            out.writeObject(listLnExp);
        }
        LinkedListTabulatedFunction deserializedListLnExp;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(externalPath))) {
            deserializedListLnExp = (LinkedListTabulatedFunction) in.readObject();
        }
        System.out.println("=== ln(exp(x)) Externalizable verification ===");
        compareAtIntegerPoints(listLnExp, deserializedListLnExp, 0.0);
    }

    private static void printFunctionValues(Function function, double left, double right, double step) {
        for (double x = left; x <= right + 1e-9; x += step) {
            System.out.printf("x=%.2f -> %.6f%n", x, function.getFunctionValue(x));
        }
    }

    private static void compareFunctions(Function reference, Function tabulated, double left, double right, double step) {
        for (double x = left; x <= right + 1e-9; x += step) {
            double analytic = reference.getFunctionValue(x);
            double tabulatedValue = tabulated.getFunctionValue(x);
            System.out.printf("x=%.2f -> analytic=%.6f; tabulated=%.6f%n", x, analytic, tabulatedValue);
        }
    }

    private static void compareAtIntegerPoints(Function expected, Function actual, double firstArgument) {
        for (int i = 0; i <= 10; i++) {
            double x = (i == 0) ? firstArgument : i;
            double expectedValue = expected.getFunctionValue(x);
            double actualValue = actual.getFunctionValue(x);
            boolean substituted = i == 0 && Double.compare(x, i) != 0;
            String label = substituted ? String.format("%.4g*", x) : Integer.toString(i);
            System.out.printf("x=%s -> expected=%.6f; actual=%.6f%n", label, expectedValue, actualValue);
        }
    }

    private static FunctionPoint[] extractPoints(TabulatedFunction function) {
        FunctionPoint[] points = new FunctionPoint[function.getPointsCount()];
        for (int i = 0; i < points.length; i++) {
            points[i] = function.getPoint(i);
        }
        return points;
    }
}
