package org.jacoco.boundary.value.coverage.core.internal.flow;

import org.jacoco.boundary.value.coverage.core.internal.instr.BoundaryValuesProbesVisitor;
import org.jacoco.core.internal.flow.ClassProbesVisitor;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.flow.LabelFlowAnalyzer;
import org.jacoco.core.internal.flow.MethodSanitizer;
import org.jacoco.core.internal.instr.IProbeInserter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ClassBvProbesVisitor extends ClassVisitor implements IProbeIdGenerator {

    private static final MethodVisitor EMPTY_METHOD_VISITOR = new MethodVisitor(InstrSupport.ASM_API_VERSION, null) {
    };

    private final IProbeInserter probeInserter;

    private int counter = 0;

    private String name;

    public ClassBvProbesVisitor(ClassVisitor cv, IProbeInserter probeInserter) {
        super(InstrSupport.ASM_API_VERSION, cv);
        this.probeInserter = probeInserter;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        final MethodVisitor methodVisitor = mv != null ? mv : EMPTY_METHOD_VISITOR;
        return new MethodSanitizer(null, access, name, descriptor, signature, exceptions) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                LabelFlowAnalyzer.markLabels(this);
                BoundaryValuesProbesVisitor bvVisitor = new BoundaryValuesProbesVisitor(
                        methodVisitor,
                        probeInserter,
                        ClassBvProbesVisitor.this);
                this.accept(bvVisitor);
            }
        };
    }

    public int getTotalProbeCount() {
        return counter;
    }

    // === IProbeIdGenerator ===
    public int nextId() {
        return counter++;
    }
}
