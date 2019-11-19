package org.jacoco.examples.boundary;

public class IntExample implements Runnable {
    public void run() {
        testLess();

        testEqual();

        testInRange();

        testForLoop();

        testMaxValue();

        testIntCmpZero();
    }

    // ----------------------------------------------------------------------

    private void testLess() {
        lessNotCovered(0, 42);
        lessNotCovered(42, 0);

        lessPartlyCovered(0, 1);
        lessPartlyCovered(42, 1);

        lessFullyCovered(0, 1);
        lessFullyCovered(1, 1);
    }

    private void lessNotCovered(int a, int b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessPartlyCovered(int a, int b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessFullyCovered(int a, int b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    // ----------------------------------------------------------------------

    private void testEqual() {
        equalNotCovered(0, 42);

        equalPartlyCovered(0, 0);

        equalFullyCovered(0, 0);
        equalFullyCovered(0, 1);
        equalFullyCovered(1, 0);
    }

    private void equalNotCovered(int a, int b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalPartlyCovered(int a, int b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalFullyCovered(int a, int b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    // ----------------------------------------------------------------------

    private void testInRange() {
        inRangeNotCovered(0, 4, 1);
        inRangeNotCovered(0, 4, 2);
        inRangeNotCovered(0, 4, -42);
        inRangeNotCovered(0, 4, 42);


        inRangePartlyCovered(0, 4, 1);
        inRangePartlyCovered(0, 4, 2);
        inRangePartlyCovered(0, 4, 3);
        inRangePartlyCovered(0, 4, 4);

        inRangeFullyCovered(0, 4, -1);
        inRangeFullyCovered(0, 4, 0);
        inRangeFullyCovered(0, 4, 4);
        inRangeFullyCovered(0, 4, 5);
    }

    private boolean inRangeNotCovered(int left, int right, int value) {
        return left <= value && value <= right;
    }

    private boolean inRangePartlyCovered(int left, int right, int value) {
        return left <= value && value <= right;
    }

    private boolean inRangeFullyCovered(int left, int right, int value) {
        return left <= value && value <= right;
    }

    // ----------------------------------------------------------------------

    private void testForLoop() {
        // forLoopNotCovered(_, _);

        forLoopPartlyCovered(1, 0);

        forLoopFullyCovered(0, 1);
    }

    private void forLoopNotCovered(int start, int end) {
        for (; start <= end; start++);
    }

    private void forLoopPartlyCovered(int start, int end) {
        for (; start <= end; start++);
    }

    private void forLoopFullyCovered(int start, int end) {
        for (; start <= end; start++);
    }


    // ----------------------------------------------------------------------

    private void testMaxValue() {
        intEqualMaxValue(Integer.MAX_VALUE - 1);
        intEqualMaxValue(Integer.MAX_VALUE);
        intEqualMaxValue(Integer.MIN_VALUE);
    }

    private void intEqualMaxValue(int a) {
        if (a == Integer.MAX_VALUE) {
            System.out.println("a == MAX_VALUE");
        }
    }


    // ----------------------------------------------------------------------

    private void testIntCmpZero() {
        intNotEqualZero(-1);
        intNotEqualZero(0);
        intNotEqualZero(1);

        intGTZero(0);
        intGTZero(1);

        intGEZero(-1);
        intGEZero(0);
    }

    private void intNotEqualZero(int a) {
        if (a != 0) {
            System.out.println("a != 0");
        }
    }

    private void intGTZero(int a) {
        if (a > 0) {
            System.out.println("a > 0");
        }
    }

    private void intGEZero(int a) {
        if (a >= 0) {
            System.out.println("a >= 0");
        }
    }

}
