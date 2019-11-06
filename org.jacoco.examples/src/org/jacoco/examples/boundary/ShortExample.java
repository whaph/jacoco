package org.jacoco.examples.boundary;

public class ShortExample implements Runnable {
    public void run() {
        testLess();

        testEqual();

        testInRange();

        testForLoop();

        testshortBugs();
    }

    // ----------------------------------------------------------------------

    private void testLess() {
        lessNotCovered((short)0, (short)42);
        lessNotCovered((short)42, (short)0);

        lessPartlyCovered((short)0, (short)1);
        lessPartlyCovered((short)42,(short) 1);

        lessFullyCovered((short)0, (short)1);
        lessFullyCovered((short)1, (short)1);
    }

    private void lessNotCovered(short a, short b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessPartlyCovered(short a, short b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessFullyCovered(short a, short b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    // ----------------------------------------------------------------------

    private void testEqual() {
        equalNotCovered((short)0, (short)42);

        equalPartlyCovered((short)0, (short)0);

        equalFullyCovered((short)0, (short)0);
        equalFullyCovered((short)0, (short)1);
        equalFullyCovered((short)1, (short)0);
    }

    private void equalNotCovered(short a, short b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalPartlyCovered(short a, short b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalFullyCovered(short a, short b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    // ----------------------------------------------------------------------

    private void testInRange() {
        inRangeNotCovered((short)0, (short)4, (short)1);
        inRangeNotCovered((short)0, (short)4, (short)2);
        inRangeNotCovered((short)0, (short)4, (short)-42);
        inRangeNotCovered((short)0, (short)4, (short)42);


        inRangePartlyCovered((short)0, (short)4, (short)1);
        inRangePartlyCovered((short)0, (short)4, (short)2);
        inRangePartlyCovered((short)0, (short)4, (short)3);
        inRangePartlyCovered((short)0, (short)4, (short)4);

        inRangeFullyCovered((short)0, (short)4, (short)-1);
        inRangeFullyCovered((short)0, (short)4, (short)0);
        inRangeFullyCovered((short)0, (short)4, (short)4);
        inRangeFullyCovered((short)0, (short)4, (short)5);
    }

    private boolean inRangeNotCovered(short left, short right, short value) {
        return left <= value && value <= right;
    }

    private boolean inRangePartlyCovered(short left, short right, short value) {
        return left <= value && value <= right;
    }

    private boolean inRangeFullyCovered(short left, short right, short value) {
        return left <= value && value <= right;
    }

    // ----------------------------------------------------------------------

    private void testForLoop() {
        // forLoopNotCovered(_, _);

        forLoopPartlyCovered((short)1, (short)0);

        forLoopFullyCovered((short)0, (short)1);
    }

    private void forLoopNotCovered(short start, short end) {
        for (; start <= end; start++);
    }

    private void forLoopPartlyCovered(short start, short end) {
        for (; start <= end; start++);
    }

    private void forLoopFullyCovered(short start, short end) {
        for (; start <= end; start++);
    }


    // ----------------------------------------------------------------------

    private void testshortBugs() {
        shortEqualZero((short) -1);
        shortEqualZero((short) 0);
        shortEqualZero((short) 1);

        testMaxValue();
    }

    private void shortEqualZero(short a) {
        if (a == 0) {
            System.out.println("a == 0");
        }
    }

    private void testMaxValue() {
        shortEqualMaxValue((short) (Short.MAX_VALUE - 1));
        shortEqualMaxValue(Short.MAX_VALUE);
        shortEqualMaxValue(Short.MIN_VALUE);
    }

    private void shortEqualMaxValue(short a) {
        if (a == Short.MAX_VALUE) {
            System.out.println("a == MAX_VALUE");
        }
    }
}
