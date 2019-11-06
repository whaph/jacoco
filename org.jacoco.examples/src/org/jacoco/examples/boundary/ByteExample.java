package org.jacoco.examples.boundary;

public class ByteExample implements Runnable {
    public void run() {
        testLess();

        testEqual();

        testInRange();

        testForLoop();

        testbyteBugs();
    }

    // ----------------------------------------------------------------------

    private void testLess() {
        lessNotCovered((byte)0, (byte)42);
        lessNotCovered((byte)42, (byte)0);

        lessPartlyCovered((byte)0, (byte)1);
        lessPartlyCovered((byte)42,(byte) 1);

        lessFullyCovered((byte)0, (byte)1);
        lessFullyCovered((byte)1, (byte)1);
    }

    private void lessNotCovered(byte a, byte b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessPartlyCovered(byte a, byte b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessFullyCovered(byte a, byte b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    // ----------------------------------------------------------------------

    private void testEqual() {
        equalNotCovered((byte)0, (byte)42);

        equalPartlyCovered((byte)0, (byte)0);

        equalFullyCovered((byte)0, (byte)0);
        equalFullyCovered((byte)0, (byte)1);
        equalFullyCovered((byte)1, (byte)0);
    }

    private void equalNotCovered(byte a, byte b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalPartlyCovered(byte a, byte b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalFullyCovered(byte a, byte b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    // ----------------------------------------------------------------------

    private void testInRange() {
        inRangeNotCovered((byte)0, (byte)4, (byte)1);
        inRangeNotCovered((byte)0, (byte)4, (byte)2);
        inRangeNotCovered((byte)0, (byte)4, (byte)-42);
        inRangeNotCovered((byte)0, (byte)4, (byte)42);


        inRangePartlyCovered((byte)0, (byte)4, (byte)1);
        inRangePartlyCovered((byte)0, (byte)4, (byte)2);
        inRangePartlyCovered((byte)0, (byte)4, (byte)3);
        inRangePartlyCovered((byte)0, (byte)4, (byte)4);

        inRangeFullyCovered((byte)0, (byte)4, (byte)-1);
        inRangeFullyCovered((byte)0, (byte)4, (byte)0);
        inRangeFullyCovered((byte)0, (byte)4, (byte)4);
        inRangeFullyCovered((byte)0, (byte)4, (byte)5);
    }

    private boolean inRangeNotCovered(byte left, byte right, byte value) {
        return left <= value && value <= right;
    }

    private boolean inRangePartlyCovered(byte left, byte right, byte value) {
        return left <= value && value <= right;
    }

    private boolean inRangeFullyCovered(byte left, byte right, byte value) {
        return left <= value && value <= right;
    }

    // ----------------------------------------------------------------------

    private void testForLoop() {
        // forLoopNotCovered(_, _);

        forLoopPartlyCovered((byte)1, (byte)0);

        forLoopFullyCovered((byte)0, (byte)1);
    }

    private void forLoopNotCovered(byte start, byte end) {
        for (; start <= end; start++);
    }

    private void forLoopPartlyCovered(byte start, byte end) {
        for (; start <= end; start++);
    }

    private void forLoopFullyCovered(byte start, byte end) {
        for (; start <= end; start++);
    }


    // ----------------------------------------------------------------------

    private void testbyteBugs() {
        byteEqualZero((byte) -1);
        byteEqualZero((byte) 0);
        byteEqualZero((byte) 1);

        testMaxValue();
    }

    private void byteEqualZero(byte a) {
        if (a == 0) {
            System.out.println("a == 0");
        }
    }

    private void testMaxValue() {
        byteEqualMaxValue((byte) (Byte.MAX_VALUE - 1));
        byteEqualMaxValue(Byte.MAX_VALUE);
        byteEqualMaxValue(Byte.MIN_VALUE);
    }

    private void byteEqualMaxValue(byte a) {
        if (a == Byte.MAX_VALUE) {
            System.out.println("a == MAX_VALUE");
        }
    }
}
