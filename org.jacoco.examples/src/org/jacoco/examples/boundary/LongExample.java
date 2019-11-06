package org.jacoco.examples.boundary;

public class LongExample implements Runnable {

    public void run() {
        testLess();

        testEqual();

        testInRange();

        testForLoop();
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

    private void lessNotCovered(long a, long b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessPartlyCovered(long a, long b) {
        if (a < b) {
            System.out.println("a < b");
        }
    }

    private void lessFullyCovered(long a, long b) {
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

    private void equalNotCovered(long a, long b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalPartlyCovered(long a, long b) {
        if (a == b) {
            System.out.println("a == b");
        }
    }

    private void equalFullyCovered(long a, long b) {
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

    private boolean inRangeNotCovered(long left, long right, long value) {
        return left <= value && value <= right;
    }

    private boolean inRangePartlyCovered(long left, long right, long value) {
        return left <= value && value <= right;
    }

    private boolean inRangeFullyCovered(long left, long right, long value) {
        return left <= value && value <= right;
    }

    // ----------------------------------------------------------------------

    private void testForLoop() {
        // forLoopNotCovered(_, _);

        forLoopPartlyCovered(1, 0);

        forLoopFullyCovered(0, 1);
    }

    private void forLoopNotCovered(long start, long end) {
        for (; start <= end; start++);
    }

    private void forLoopPartlyCovered(long start, long end) {
        for (; start <= end; start++);
    }

    private void forLoopFullyCovered(long start, long end) {
        for (; start <= end; start++);
    }

}
