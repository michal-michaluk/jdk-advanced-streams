package dev.bottega.streams.almostlikefunctional;

import org.junit.jupiter.api.Test;

import java.util.function.*;

public class FunctionalInterfacesTest {

    @Test
    public void javaUtilFunctional() throws Exception {
        Supplier<Boolean> supplier = () -> Boolean.FALSE;
        Predicate<String> predicate = (String s) -> false;
        Consumer<String> consumer = (String o) -> {};
        Function<String, String> function = (String val) -> val + "!";
        BinaryOperator<String> binaryOperator = (String o, String o2) -> "";
        UnaryOperator<String> unaryOperator = (String s) -> s;

        BiConsumer<String, String> biConsumer = (String o, String o2) -> {};
        BiFunction<String, String, String> biFunction = (String o, String o2) -> "";
        BiPredicate<String, String> biPredicate = (String o, String o2) -> true;
    }

    @Test
    public void booleanPrimitive() throws Exception {
        BooleanSupplier booleanSupplier = () -> false;
    }

    @Test
    public void doublePrimitive() throws Exception {
        DoubleSupplier doubleSupplier = () -> 44.;
        DoublePredicate doublePredicate = (double value) -> false;
        DoubleConsumer doubleConsumer = (double value) -> {};
        ObjDoubleConsumer<String> objDoubleConsumer = (String o, double value) -> {};
        DoubleFunction doubleFunction = (double value) -> 2*value;

        ToDoubleFunction<String> toDoubleFunction = (String value) -> 2.1;
        ToDoubleBiFunction<String, String> toDoubleBiFunction = (String s, String s2) -> 2.1;
        DoubleToIntFunction doubleToIntFunction = (double value) -> (int) value;
        DoubleToLongFunction doubleToLongFunction = (double value) -> (long) value;

        DoubleUnaryOperator doubleUnaryOperator = (double operand) -> operand / 2;
        DoubleBinaryOperator doubleBinaryOperator = (double left, double right) -> left;
    }

    @Test
    public void intPrimitive() throws Exception {
        IntSupplier intSupplier = () -> 44;
        IntPredicate intPredicate = (int value) -> false;
        IntConsumer intConsumer = (int value) -> {};
        ObjIntConsumer<String> objIntConsumer = (String o, int value) -> {};
        IntFunction intFunction = (int value) -> 2 * value;

        ToIntFunction<String> toIntFunction = (String value) -> 2;
        ToIntBiFunction<String, String> toIntBiFunction = (String s, String s2) -> 2;
        IntToDoubleFunction intToDoubleFunction = (int value) -> (int) value;
        IntToLongFunction intToLongFunction = (int value) -> (long) value;

        IntUnaryOperator intUnaryOperator = (int operand) -> operand / 2;
        IntBinaryOperator intBinaryOperator = (int left, int right) -> left;
    }

    @Test
    public void longPrimitive() throws Exception {
        LongSupplier longSupplier = () -> 44L;
        LongPredicate longPredicate = (long value) -> false;
        LongConsumer longConsumer = (long value) -> {};
        ObjLongConsumer<String> objLongConsumer = (String o, long value) -> {};
        LongFunction longFunction = (long value) -> 2 * value;

        ToLongBiFunction<String, String> toLongBiFunction = (String s, String s2) -> 1L;
        ToLongFunction<String> toLongFunction = (String value) -> 1L;
        LongToDoubleFunction longToDoubleFunction = (long value) -> value * 1.3;
        LongToIntFunction longToIntFunction = (long value) -> (int) value;

        LongUnaryOperator longUnaryOperator = (long operand) -> operand / 2;
        LongBinaryOperator longBinaryOperator = (long left, long right) -> left;
    }
}
