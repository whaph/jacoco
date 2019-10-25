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
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * A {@link MethodVisitor} with additional methods to get probe insertion
 * information.
 */
public abstract class MethodProbesVisitor extends MethodVisitor {

    private final LastInstructionMemorizerMethodVisitor memorizer;

    /**
     * New visitor instance without delegate visitor.
     */
    public MethodProbesVisitor() {
        this(null);
    }

    /**
     * New visitor instance that delegates to the given visitor.
     *
     * @param mv optional next visitor in chain
     */
    public MethodProbesVisitor(final MethodVisitor mv) {
        super(InstrSupport.ASM_API_VERSION, null);
        this.memorizer = new LastInstructionMemorizerMethodVisitor(mv);
        this.mv = this.memorizer;
    }

    /**
     * Visits an unconditional probe that should be inserted at the current
     * position.
     *
     * @param probeId id of the probe to insert
     */
    @SuppressWarnings("unused")
    public void visitProbe(final int probeId) {
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.LCMP) {
            memorizer.setLastInstruction(new InsnNode(opcode));
        } else {
            super.visitInsn(opcode);
        }
    }

    void visitLCMP() {
        super.visitInsn(Opcodes.LCMP);
    }

    /**
     * Visits a jump instruction. A probe with the given id should be inserted
     * in a way that it is executed only when the jump to the given label is
     * executed.
     *
     * @param opcode  the opcode of the type instruction to be visited. This opcode
     *                is either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
     *                IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
     *                IF_ACMPEQ, IF_ACMPNE, GOTO, IFNULL or IFNONNULL.
     * @param label   the operand of the instruction to be visited. This operand is
     *                a label that designates the instruction to which the jump
     *                instruction may jump.
     * @param probeId id of the probe
     * @param frame   stackmap frame status after the execution of the jump
     *                instruction. The instance is only valid with the call of this
     *                method.
     * @see MethodVisitor#visitJumpInsn(int, Label)
     */
    @SuppressWarnings("unused")
    public void visitJumpInsnWithProbe(final int opcode, final Label label,
                                       final int probeId, final IFrame frame) {
    }

    /**
     * Visits a jump instruction for ints that is identified to have a boundary.
     * Probes with the given id should be inserted in a way that it is executed
     * only when the two operands are neighbouring boundary values.
     *
     * @param opcode   the opcode of the type instruction to be visited. This opcode
     *                 is either IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, or
     *                 if the preceding instruction is LCMP or pushes a true int
     *                 on the stack (to compare with zero), then either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE.
     * @param label    the operand of the instruction to be visited. This operand is
     *                 a label that designates the instruction to which the jump
     *                 instruction may jump.
     * @param probeIds ids of the probes.
     * @param frame    stackmap frame status after the execution of the jump instructions.
     *                 The instance is only valid with the call of this method.
     * @see MethodVisitor#visitJumpInsn(int, Label)
     */
    public void visitBoundaryInsnWithProbes(final int opcode, final Label label,
                                            final int[] probeIds, final IFrame frame) {
    }

    /**
     * Visits a jump instruction for longs that is identified to have a boundary.
     * Probes with the given id should be inserted in a way that it is executed
     * only when the two operands are neighbouring boundary values.
     *
     * Should only be called in {@link MethodVisitor#visitInsn(int)} where the opcode is LCMP.
     *
     * @param opcode   the opcode of the type instruction to be visited. This opcode
     *                 is either IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, or
     *                 if the succeeding instruction is LCMP or pushes a true int
     *                 on the stack (to compare with zero), then either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE.
     * @param probeIds ids of the probes.
     * @param frame    stackmap frame status after the execution of the jump instructions.
     *                 The instance is only valid with the call of this method.
     * @see MethodVisitor#visitInsn(int)
     */
    public void visitLongBoundaryInsnWithProbes(final int opcode, final Label label, final int[] probeIds, final IFrame frame) {
    }
    /**
     * Visits a zero operand instruction with a probe. This event is used only
     * for instructions that terminate the method. Therefore the probe must be
     * inserted before the actual instruction.
     *
     * @param opcode  the opcode of the instruction to be visited. This opcode is
     *                either IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN or
     *                ATHROW.
     * @param probeId id of the probe
     * @see MethodVisitor#visitInsn(int)
     */
    @SuppressWarnings("unused")
    public void visitInsnWithProbe(final int opcode, final int probeId) {
    }

    /**
     * Visits a TABLESWITCH instruction with optional probes for each target
     * label. Implementations can be optimized based on the fact that the same
     * target labels will always have the same probe id within a call to this
     * method. The probe id for each label can be obtained with
     * {@link LabelInfo#getProbeId(Label)}.
     *
     * @param min    the minimum key value.
     * @param max    the maximum key value.
     * @param dflt   beginning of the default handler block.
     * @param labels beginnings of the handler blocks. <code>labels[i]</code> is
     *               the beginning of the handler block for the
     *               <code>min + i</code> key.
     * @param frame  stackmap frame status after the execution of the switch
     *               instruction. The instance is only valid with the call of this
     *               method.
     * @see MethodVisitor#visitTableSwitchInsn(int, int, Label, Label[])
     * @see MethodProbesVisitor#isLastInsnTrueIntInsn()
     */
    @SuppressWarnings("unused")
    public void visitTableSwitchInsnWithProbes(final int min, final int max,
                                               final Label dflt, final Label[] labels, final IFrame frame) {
    }

    /**
     * Visits a LOOKUPSWITCH instruction with optional probes for each target
     * label. Implementations can be optimized based on the fact that the same
     * target labels will always have the same probe id within a call to this
     * method. The probe id for each label can be obtained with
     * {@link LabelInfo#getProbeId(Label)}.
     *
     * @param dflt   beginning of the default handler block.
     * @param keys   the values of the keys.
     * @param labels beginnings of the handler blocks. <code>labels[i]</code> is
     *               the beginning of the handler block for the
     *               <code>keys[i]</code> key.
     * @param frame  stackmap frame status after the execution of the switch
     *               instruction. The instance is only valid with the call of this
     *               method.
     * @see MethodVisitor#visitLookupSwitchInsn(Label, int[], Label[])
     */
    @SuppressWarnings("unused")
    public void visitLookupSwitchInsnWithProbes(final Label dflt,
                                                final int[] keys, final Label[] labels, final IFrame frame) {
    }

    /**
     * This method can be overwritten to hook into the process of emitting the
     * instructions of this method as <code>visitX()</code> events.
     *
     * @param methodNode    the content to emit
     * @param methodVisitor A visitor to emit the content to. Note that this is not
     *                      necessarily this visitor instance but some wrapper which
     *                      calculates the probes.
     */
    public void accept(final MethodNode methodNode,
                       final MethodVisitor methodVisitor) {
        methodNode.accept(methodVisitor);
    }

    /**
     * Checks if the last instruction leaves an (<a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.11.1-320">actual type</a>)
     * int on the stack.
     *
     * @return <code>true</code> if last instruction opcode is D2I, F2I, L2I, IADD, ISUB, IDIV, IREM, IMUL, ISHL, ISHR,
     * IOR, IXOR, IAND, INEG, ILOAD, IALOAD or getting an int field or calling a method that returns an int, else <code>false</code>
     */
    boolean isLastInsnTrueIntInsn() {
        AbstractInsnNode lastInstruction = memorizer.getLastInstruction();
        if (lastInstruction == null) {
            return false;
        }

        // ICONST_<n>, bipush and sipush are not part of it since comparing a constant against a zero constant is simply stupid
        // and in the case of ICONST_0 and ICONST_1 it is possible that those are meant as booleans and not as numbers
        switch (lastInstruction.getOpcode()) {
        case Opcodes.D2I:
        case Opcodes.F2I:
        case Opcodes.L2I:
        case Opcodes.IADD:
        case Opcodes.ISUB:
        case Opcodes.IDIV:
        case Opcodes.IREM:
        case Opcodes.IMUL:
        case Opcodes.ISHL:
        case Opcodes.ISHR:
        case Opcodes.INEG:
        case Opcodes.ILOAD:
        case Opcodes.IALOAD:
        // TODO kann auch fuer booleans verwendet werden
        case Opcodes.IAND:
        case Opcodes.IXOR:
        case Opcodes.IOR:
            return true;
        }

        if (lastInstruction instanceof FieldInsnNode) {
            switch (lastInstruction.getOpcode()) {
                case Opcodes.GETSTATIC:
                case Opcodes.GETFIELD:
                    return ((FieldInsnNode) lastInstruction).desc.equals("I");

                default:
                    return false;
            }
        }

        if (lastInstruction instanceof MethodInsnNode) {
            switch (lastInstruction.getOpcode()) {
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE:
                    return Type.getReturnType(((MethodInsnNode) lastInstruction).desc).getDescriptor().equals("I");
                default:
                    return false;
            }
        }

        if (lastInstruction instanceof InvokeDynamicInsnNode) {
            return Type.getReturnType(((InvokeDynamicInsnNode) lastInstruction).desc).getDescriptor().equals("I");
        }

        return false;
    }

    boolean isLastInsnLCMP() {
        AbstractInsnNode lastInstruction = memorizer.getLastInstruction();
        if (lastInstruction == null) {
            return false;
        }
        return lastInstruction.getOpcode() == Opcodes.LCMP;
    }
}
