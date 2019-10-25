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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * A visitor that memorizes the last instruction visited.
 * <p>
 * (Note that the {@link AbstractInsnNode Instruction} being memorized is only a mock up of its original.
 * <p>
 * Additionally the {@link org.objectweb.asm.Opcodes#NOP NOP-Instruction} won't be memorized, but still committed to the next
 * {@link org.objectweb.asm.MethodVisitor MethodVisitor} in the chain.)
 */
public class LastInstructionMemorizerMethodVisitor extends MethodVisitor {
    private AbstractInsnNode lastInstruction = null;

    public LastInstructionMemorizerMethodVisitor(final MethodVisitor methodVisitor) {
        super(InstrSupport.ASM_API_VERSION, methodVisitor);
    }

    /**
     * Getter for the last instruction.
     *
     * @return the last instruction or <code>null</code>.
     */
    AbstractInsnNode getLastInstruction() {
        return lastInstruction;
    }
    void setLastInstruction(AbstractInsnNode lastInstruction) {
        // TODO sollte irgendwann auch ohne gehen
        this.lastInstruction = lastInstruction;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode != Opcodes.NOP) {
            lastInstruction = new InsnNode(opcode);
        }

        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        lastInstruction = new MethodInsnNode(opcode, owner, name, descriptor, isInterface);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        lastInstruction = new FieldInsnNode(opcode, owner, name, descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        lastInstruction = new IincInsnNode(var, increment);
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        lastInstruction = new IntInsnNode(opcode, operand);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        lastInstruction = new MultiANewArrayInsnNode(descriptor, numDimensions);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitLdcInsn(Object value) {
        lastInstruction = new LdcInsnNode(value);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        lastInstruction = new TypeInsnNode(opcode, type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        lastInstruction = new VarInsnNode(opcode, var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        lastInstruction = new InvokeDynamicInsnNode(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        lastInstruction = new JumpInsnNode(opcode, null);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        lastInstruction = new TableSwitchInsnNode(min, max, null, (LabelNode[]) null);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        lastInstruction = new LookupSwitchInsnNode(null, keys, null);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }
}
