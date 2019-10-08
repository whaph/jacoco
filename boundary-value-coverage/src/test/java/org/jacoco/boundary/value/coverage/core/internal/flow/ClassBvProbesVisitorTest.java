package org.jacoco.boundary.value.coverage.core.internal.flow;

import org.jacoco.boundary.value.coverage.core.internal.instr.BoundaryValuesProbesVisitor;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.IProbeInserter;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

import static org.junit.Assert.*;

public class ClassBvProbesVisitorTest {


    private static final IProbeInserter NULL_PROBE_INSERTER = new IProbeInserter() {

        public void insertProbe(int id) {

        }
    };

    @Test
    public void testProbeCounter() {

        ClassBvProbesVisitor bvVisitor = new ClassBvProbesVisitor(null, NULL_PROBE_INSERTER);
        assertEquals(0, bvVisitor.nextId());
        assertEquals(1, bvVisitor.nextId());
        assertEquals(2, bvVisitor.nextId());

        assertEquals(3, bvVisitor.getTotalProbeCount());
    }
}