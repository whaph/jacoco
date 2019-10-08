package org.jacoco.boundary.value.coverage.core.internal.intr;

import org.jacoco.boundary.value.coverage.core.internal.instr.BoundaryValuesProbesVisitor;
import org.jacoco.core.instr.MethodRecorder;
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.instr.IProbeInserter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.assertEquals;


public class BoundaryValuesProbesVisitorTest {

    private BoundaryValuesProbesVisitor visitor;
    private MethodRecorder actual, expected;
    private MethodVisitor actualVisitor, expectedVisitor;
    private IFrame frame;

    @Before
    public void setUp() {
        actual = new MethodRecorder();
        actualVisitor = actual.getVisitor();
        expected = new MethodRecorder();
        expectedVisitor = expected.getVisitor();
        final IProbeInserter probeInserter = new IProbeInserter() {
            public void insertProbe(int id) {
                actual.getVisitor().visitLdcInsn("Probe " + id);
            }
        };


        visitor = new BoundaryValuesProbesVisitor(actualVisitor, probeInserter, new IProbeIdGenerator() {
            int id = 0;
            public int nextId() {
                return id++;
            }
        });

        frame = new IFrame() {
            public void accept(MethodVisitor mv) {
                mv.visitFrame(Opcodes.F_FULL, 0, null, 0, null);
            }
        };
    }

    @After
    public void verify() {
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPNE() {
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.ISUB);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.IADD);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 2");
        expectedVisitor.visitLabel(l2);
        // middle bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l3);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l3);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, new Label());
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPEQ() {
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.ISUB);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.IADD);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 2");
        expectedVisitor.visitLabel(l2);
        // middle bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l3);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l3);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, new Label());
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPLT() {
        Label l1 = new Label();
        Label l2 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPLT, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.ISUB);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPLT, new Label());
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPGE() {
        Label l1 = new Label();
        Label l2 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPGE, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.ISUB);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, new Label());
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPLE() {
        Label l1 = new Label();
        Label l2 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPLE, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.IADD);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPLE, new Label());
    }

    @Test
    public void testVisitJumpInsn_IF_ICMPGT() {
        Label l1 = new Label();
        Label l2 = new Label();

        visitor.visitJumpInsn(Opcodes.IF_ICMPGT, l1);

        // left bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
        expectedVisitor.visitLdcInsn("Probe 0");
        expectedVisitor.visitLabel(l1);
        // right bv
        expectedVisitor.visitInsn(Opcodes.DUP2);
        expectedVisitor.visitInsn(Opcodes.ICONST_1);
        expectedVisitor.visitInsn(Opcodes.IADD);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, l2);
        expectedVisitor.visitLdcInsn("Probe 1");
        expectedVisitor.visitLabel(l2);
        expectedVisitor.visitJumpInsn(Opcodes.IF_ICMPGT, new Label());
    }

}