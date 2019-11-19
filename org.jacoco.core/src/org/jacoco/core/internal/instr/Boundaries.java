/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Helper class to generate conditional probes for boundary values.
 */
final class Boundaries {
    private final MethodVisitor mv;
    private final IProbeInserter probeInserter;

    public Boundaries(final MethodVisitor mv, final IProbeInserter probeInserter) {
        this.mv = mv;
        this.probeInserter = probeInserter;
    }

    int visitBoundaryInsnWithProbes(int opcode, int[] probeIds) {
        switch (opcode) {
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
                ensureProbes(probeIds, 2);
                return visitCheckEQNE(probeIds[0], probeIds[1]);
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ICMPGT:
                ensureProbes(probeIds, 2);
                return visitCheckLEGT(probeIds[0], probeIds[1]);
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
                ensureProbes(probeIds, 2);
                return visitCheckLTGE(probeIds[0], probeIds[1]);
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
                ensureProbes(probeIds, 2);
                return visitCheckIntCmpZero(probeIds[0], probeIds[1]);
            case Opcodes.IFLE:
            case Opcodes.IFGT:
                ensureProbes(probeIds, 2);
                return visitCheckIntLEGTZero(probeIds[0], probeIds[1]);
            case Opcodes.IFLT:
            case Opcodes.IFGE:
                ensureProbes(probeIds, 2);
                return visitCheckIntLTGEZero(probeIds[0], probeIds[1]);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     *
     * The stack size of the operand stack is increased by 2.
     *
     * @param leftOrRightBvProbeId
     * @param midBvProbeId
     */
    private int visitCheckIntCmpZero(int leftOrRightBvProbeId, int midBvProbeId) {
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        mv.visitInsn(Opcodes.ICONST_M1); // -1
        // Stack[0] : I
        // Stack[1] : I
        // Stack[2] : I

        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, intermediate); // a == -1; LEFT
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        mv.visitInsn(Opcodes.ICONST_1); // 1
        // Stack[0] : I
        // Stack[1] : I
        // Stack[2] : I
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, intermediate); // a == 1; RIGHT
        // Stack[0] : I
        Label skipProbe = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, skipProbe);
        mv.visitLabel(intermediate);
        // Stack[0] : I
        probeInserter.insertProbe(leftOrRightBvProbeId);
        mv.visitLabel(skipProbe);
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I

        intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, intermediate);
        // Stack[0] : I
        probeInserter.insertProbe(midBvProbeId); // MIDDLE a == 0
        mv.visitLabel(intermediate);
        return 2;
    }

    /**
     *
     * The stack size of the operand stack is increased by 2.
     *
     * @param leftBvProbeId
     * @param rightBvProbeId
     */
    private int visitCheckIntLEGTZero(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        Label skipLeft = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, skipLeft);
        // Stack[0] : I
        probeInserter.insertProbe(leftBvProbeId); // a == 0; LEFT
        mv.visitLabel(skipLeft);

        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        mv.visitInsn(Opcodes.ICONST_1);
        // Stack[0] : I
        // Stack[1] : I
        // Stack[2] : I
        Label skipRight = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, skipRight); // a == 1; RIGHT
        probeInserter.insertProbe(rightBvProbeId);
        mv.visitLabel(skipRight);
        // Stack[0] : I

        return 2;
    }

    private int visitCheckIntLTGEZero(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        mv.visitInsn(Opcodes.ICONST_M1);
        // Stack[0] : I
        // Stack[1] : I
        // Stack[2] : I
        Label skipLeft = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, skipLeft);
        // Stack[0] : I
        probeInserter.insertProbe(leftBvProbeId); // a == -1; LEFT
        mv.visitLabel(skipLeft);
        // Stack[0] : I
        mv.visitInsn(Opcodes.DUP);
        // Stack[0] : I
        // Stack[1] : I
        Label skipRight = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, skipRight); // a == 0; RIGHT
        probeInserter.insertProbe(rightBvProbeId);
        mv.visitLabel(skipRight);
        // Stack[0] : I

        return 2;
    }

    private int visitCheckEQNE(int leftOrRightBvProbeId, int midBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitLeftOrRightBv(leftOrRightBvProbeId); // a == b - 1; LEFT or a == b + 1; RIGHT
        visitBv(midBvProbeId); // a == b; MIDDLE
        // Stack[0]: I
        // Stack[1]: I
        return 3;
    }

    private int visitCheckLEGT(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitBv(leftBvProbeId); // a == b; LEFT
        visitBv(rightBvProbeId, Opcodes.IADD); // a == b + 1; RIGHT
        // Stack[0]: I
        // Stack[1]: I
        return 3;
    }

    private int visitCheckLTGE(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitBv(leftBvProbeId, Opcodes.ISUB); // a == b - 1; LEFT
        visitBv(rightBvProbeId); // a == b; RIGHT
        // Stack[0]: I
        // Stack[1]: I
        return 3;
    }

    /**
     *
     * The stack size of the operand stack is increased by 3.
     *
     * @param bvProbeId
     * @param opcode
     */
    private void visitBv(int bvProbeId, int opcode) {
        // Stack[0]: I
        // Stack[1]: I
        mv.visitInsn(Opcodes.DUP2);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        mv.visitInsn(Opcodes.ICONST_1);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        // Stack[4]: I
        mv.visitInsn(opcode);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, intermediate);

        // Stack[0]: I
        // Stack[1]: I
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(intermediate);
    }

    /**
     *
     * Inserts code to check if either the left OR right boundary value is being used.
     *
     * The stack size of the operand stack is increased by 3.
     *
     * @param bvProbeId the id of the left or right boundary value check
     * @see Boundaries#visitCheckIntCmpZero(int, int)
     * @see Boundaries#visitCheckEQNE(int, int)
     */
    private void visitLeftOrRightBv(int bvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        mv.visitInsn(Opcodes.DUP2);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        mv.visitInsn(Opcodes.ICONST_1);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        // Stack[4]: I
        mv.visitInsn(Opcodes.ISUB); // b - 1

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, intermediate);

        // Stack[0]: I
        // Stack[1]: I
        mv.visitInsn(Opcodes.DUP2);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        mv.visitInsn(Opcodes.ICONST_1);

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        // Stack[4]: I
        mv.visitInsn(Opcodes.IADD); // b + 1

        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, intermediate);

        // Stack[0]: I
        // Stack[1]: I
        Label skipProbe = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, skipProbe);

        // Stack[0]: I
        // Stack[1]: I
        mv.visitLabel(intermediate);
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(skipProbe);
    }

    /**
     *
     * The stack size of the operand stack is increased by 2.
     *
     * @param bvProbeId
     */
    private void visitBv(int bvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        mv.visitInsn(Opcodes.DUP2);
        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, intermediate);
        // Stack[0]: I
        // Stack[1]: I
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(intermediate);
    }

    int visitLongBoundaryInsnWithProbes(int opcode, int[] probeIds) {
        switch (opcode) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
                ensureProbes(probeIds, 2);
                return visitCheckLongEQNE(probeIds[0], probeIds[1]);
            case Opcodes.IFLE:
            case Opcodes.IFGT:
                ensureProbes(probeIds, 2);
                return visitCheckLongLEGT(probeIds[0], probeIds[1]);
            case Opcodes.IFLT:
            case Opcodes.IFGE:
                ensureProbes(probeIds, 2);
                return visitCheckLongLTGE(probeIds[0], probeIds[1]);
            default:
                throw new IllegalArgumentException();
        }
    }

    private int visitCheckLongEQNE(int leftOrRightBvProbeId, int midBvProbeId) {
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitLeftOrRightBvLong(leftOrRightBvProbeId); // a == b - 1; LEFT or a == b +1; RIGHT
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitBvLong(midBvProbeId); // a == b; MIDDLE
        // Stack[0/1] : L
        // Stack[2/3] : L
        return 6;
    }

    private int visitCheckLongLEGT(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitBvLong(leftBvProbeId); // a == b; LEFT
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitBvLong(rightBvProbeId, Opcodes.LADD); // a == b + 1; RIGHT
        // Stack[0/1] : L
        // Stack[2/3] : L
        return 6;
    }

    private int visitCheckLongLTGE(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitBvLong(leftBvProbeId, Opcodes.LSUB); // a == b + 1; LEFT
        // Stack[0/1] : L
        // Stack[2/3] : L
        visitBvLong(rightBvProbeId); // a == b; RIGHT
        // Stack[0/1] : L
        // Stack[2/3] : L
        return 6;
    }

    /**
     *
     * The stack size of the operand stack is increased by 4.
     *
     * @param bvProbeId
     */
    private void visitBvLong(int bvProbeId) {
        InstrSupport.dup4(mv);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        mv.visitInsn(Opcodes.LCMP);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5]: I
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, intermediate);
        // Stack[0/1] : L
        // Stack[2/3] : L
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(intermediate);
        // Stack[0/1] : L
        // Stack[2/3] : L
    }

    /**
     *
     * The stack size of the operand stack is increased by 6.
     *
     * @param bvProbeId
     * @param opcode
     */
    private void visitBvLong(int bvProbeId, int opcode) {
        // Stack[0/1] : L
        // Stack[2/3] : L
        InstrSupport.dup4(mv);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5]: L
        // Stack[6/7]: L
        mv.visitInsn(Opcodes.LCONST_1);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        // Stack[8/9] : L
        mv.visitInsn(opcode);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        mv.visitInsn(Opcodes.LCMP);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5]: L
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, intermediate);
        // Stack[0/1] : L
        // Stack[2/3] : L
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(intermediate);
        // Stack[0/1] : L
        // Stack[2/3] : L
    }

    private void visitLeftOrRightBvLong(int leftOrRightBvProbeId) {
        // Stack[0/1] : L
        // Stack[2/3] : L
        InstrSupport.dup4(mv);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        mv.visitInsn(Opcodes.LCONST_1);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        // Stack[8/9] : L
        mv.visitInsn(Opcodes.LSUB); // a == b - 1; LEFT
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        mv.visitInsn(Opcodes.LCMP);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5]: L
        Label intermediate = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, intermediate);

        InstrSupport.dup4(mv);
        // Stack[0/1] : L
        // Stack[2/3] : L
        // Stack[4/5] : L
        // Stack[6/7] : L
        mv.visitInsn(Opcodes.LCONST_1);

        mv.visitInsn(Opcodes.LADD); // a == b + 1; RIGHT
        mv.visitInsn(Opcodes.LCMP);

        mv.visitJumpInsn(Opcodes.IFEQ, intermediate);

        Label skipProbe = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, skipProbe);
        // Stack[0/1] : L
        // Stack[2/3] : L
        mv.visitLabel(intermediate);
        probeInserter.insertProbe(leftOrRightBvProbeId);
        mv.visitLabel(skipProbe);
        // Stack[0/1] : L
        // Stack[2/3] : L
    }

    private void ensureProbes(int[] probeIds, int expectedLength) {
        if (probeIds.length != expectedLength) {
            throw new IllegalArgumentException(String.format("expected %d probe ids", expectedLength));
        }
    }
}
