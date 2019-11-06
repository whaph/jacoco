package org.jacoco.examples.boundary;

public class BooleanExample implements Runnable {
    public void run() {
        final boolean b = true;
        ifTrue(b, "true");

        final boolean b1 = false;
        ifTrue(b1, "false");

        ifTrue(b && b1, "true && false");
    }

    private void ifTrue(boolean b, String s) {
        if (b) {
            System.out.println(s);
        }
    }
}
