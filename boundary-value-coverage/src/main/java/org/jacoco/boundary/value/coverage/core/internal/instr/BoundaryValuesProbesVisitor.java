package org.jacoco.boundary.value.coverage.core.internal.instr;

import org.jacoco.core.internal.flow.*;
import org.jacoco.core.internal.instr.IProbeInserter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class BoundaryValuesProbesVisitor extends MethodVisitor {
    private final IProbeInserter probeInserter;
    private final IProbeIdGenerator idGenerator;
    private AnalyzerAdapter analyzer;

    public BoundaryValuesProbesVisitor(final MethodVisitor mv,
                                       final IProbeInserter probeInserter,
                                       final IProbeIdGenerator idGenerator) {
        super(InstrSupport.ASM_API_VERSION, mv);
        this.probeInserter = probeInserter;
        this.idGenerator = idGenerator;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (isCmpInsn(opcode)) {
            // jumps are the opposite of what is in the source code
            switch (opcode) {
                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPNE:
                    visit_IF_ICMPEQ(idGenerator.nextId(), idGenerator.nextId(), idGenerator.nextId());
                    break;
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ICMPGT:
                    visit_IF_ICMPGT(idGenerator.nextId(), idGenerator.nextId());
                    break;
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ICMPGE:
                    visit_IF_ICMPGE(idGenerator.nextId(), idGenerator.nextId());
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        mv.visitJumpInsn(opcode, label);
    }

    /**
     * @param leftBvProbeId  left border value probe id
     * @param midBvProbeId   middle border value probe id
     * @param rightBvProbeId right border value probe id
     */
    protected void visit_IF_ICMPEQ(int leftBvProbeId, int midBvProbeId, int rightBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitBv(leftBvProbeId, Opcodes.ISUB); // a == b - 1
        visitBv(rightBvProbeId, Opcodes.IADD); // a == b + 1
        visitBv(midBvProbeId); // a == b
        // Stack[0]: I
        // Stack[1]: I
    }

    protected void visit_IF_ICMPGE(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitBv(leftBvProbeId, Opcodes.ISUB); // a == b - 1
        visitBv(rightBvProbeId); // a == b
        // Stack[0]: I
        // Stack[1]: I
    }

    protected void visit_IF_ICMPGT(int leftBvProbeId, int rightBvProbeId) {
        // Stack[0]: I
        // Stack[1]: I
        visitBv(leftBvProbeId); // a == b
        visitBv(rightBvProbeId, Opcodes.IADD); // a == b + 1
        // Stack[0]: I
        // Stack[1]: I
    }

    protected void visitLcmp(int leftBvProbeId, int midBvProbeId, int rightBvProbeId) {
        // TODO
    }

    private void visitBv(int bvProbeId) {
        Label intermediate = new Label();
        // Stack[0]: I
        // Stack[1]: I
        mv.visitInsn(Opcodes.DUP2);
        // Stack[0]: I
        // Stack[1]: I
        // Stack[2]: I
        // Stack[3]: I
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, intermediate);
        // Stack[0]: I
        // Stack[1]: I
        probeInserter.insertProbe(bvProbeId);
        mv.visitLabel(intermediate);
    }


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
     * Tells if the opcodes is of the if_icmp{@literal <cond>} family.
     *
     * @param opcode the opcode
     * @return <code>true</code> if int compare jump, else <code>false</code>.
     */
    private boolean isCmpInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
                return true;

            // bei long: LCMP +
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFLE:
            case Opcodes.IFGT:
            case Opcodes.IFGE:
                // fuers erste werden nur ints geprueft
                return false;
            default:
                return false;
        }
    }

}
