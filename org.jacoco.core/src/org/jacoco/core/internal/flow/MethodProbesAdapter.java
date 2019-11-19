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

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * Adapter that creates additional visitor events for probes to be inserted into
 * a method.
 */
public final class MethodProbesAdapter extends MethodVisitor {

    private final MethodProbesVisitor probesVisitor;

    private final IProbeIdGenerator idGenerator;

    private AnalyzerAdapter analyzer;

    private final Map<Label, Label> tryCatchProbeLabels;

    private final boolean instrumentBoundaryChecks;
    private boolean isLastInsnLCMP = false;

    /**
     * Create a new adapter instance.
     *
     * @param probesVisitor visitor to delegate to
     * @param idGenerator   generator for unique probe ids
     */
    public MethodProbesAdapter(final MethodProbesVisitor probesVisitor,
                               final IProbeIdGenerator idGenerator,
                               final boolean instrumentBoundaryChecks) {
        super(InstrSupport.ASM_API_VERSION, probesVisitor);
        this.probesVisitor = probesVisitor;
        this.idGenerator = idGenerator;
        this.tryCatchProbeLabels = new HashMap<Label, Label>();
        this.instrumentBoundaryChecks = instrumentBoundaryChecks;
    }

    /**
     * Create a new adapter instance.
     *
     * @param probesVisitor visitor to delegate to
     * @param idGenerator   generator for unique probe ids
     */
    public MethodProbesAdapter(final MethodProbesVisitor probesVisitor,
                               final IProbeIdGenerator idGenerator) {
        this(probesVisitor, idGenerator, false);
    }

    /**
     * If an analyzer is set {@link IFrame} handles are calculated and emitted
     * to the probes methods.
     *
     * @param analyzer optional analyzer to set
     */
    public void setAnalyzer(final AnalyzerAdapter analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end,
                                   final Label handler, final String type) {
        probesVisitor.visitTryCatchBlock(getTryCatchLabel(start), getTryCatchLabel(end),
                handler, type);
    }

    private Label getTryCatchLabel(Label label) {
        if (tryCatchProbeLabels.containsKey(label)) {
            label = tryCatchProbeLabels.get(label);
        } else if (LabelInfo.needsProbe(label)) {
            // If a probe will be inserted before the label, we'll need to use a
            // different label to define the range of the try-catch block.
            final Label probeLabel = new Label();
            LabelInfo.setSuccessor(probeLabel);
            tryCatchProbeLabels.put(label, probeLabel);
            label = probeLabel;
        }
        return label;
    }

    @Override
    public void visitLabel(final Label label) {
        if (LabelInfo.needsProbe(label)) {
            if (tryCatchProbeLabels.containsKey(label)) {
                probesVisitor.visitLabel(tryCatchProbeLabels.get(label));
            }
            probesVisitor.visitProbe(idGenerator.nextId());
        }
        probesVisitor.visitLabel(label);
    }

    @Override
    public void visitInsn(final int opcode) {
        switch (opcode) {
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                probesVisitor.visitInsnWithProbe(opcode, idGenerator.nextId());
                break;
            case Opcodes.LCMP:
                isLastInsnLCMP = true;
                break;
            default:
                probesVisitor.visitInsn(opcode);
                break;
        }
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        visitBoundary(opcode, label);
        if (LabelInfo.isMultiTarget(label)) {
            probesVisitor.visitJumpInsnWithProbe(opcode, label,
                    idGenerator.nextId(), frame(jumpPopCount(opcode)));
        } else {
            probesVisitor.visitJumpInsn(opcode, label);
        }
    }

    private void visitBoundary(final int opcode, final Label label) {
        if (!instrumentBoundaryChecks) {
            if (isLastInsnLCMP) {
                isLastInsnLCMP = false;
                probesVisitor.visitInsn(Opcodes.LCMP);
            }
            return;
        }

        if (isLastInsnLCMP) {
            isLastInsnLCMP = false;
            if (isBranchCompareZero(opcode)) {
                visitLongBoundary(opcode, label);
            }
            probesVisitor.visitInsn(Opcodes.LCMP);
        } else if (isIF_ICMPxx(opcode) || isBranchCompareZero(opcode)){
            probesVisitor.visitBoundaryInsnWithProbes(opcode, label, getProbes(), frame(jumpPopCount(opcode)));
        }
    }

    private void visitLongBoundary(int opcode, Label label) {
        probesVisitor.visitLongBoundaryInsnWithProbes(opcode, label, getProbes(), frame(1));
    }

    private int[] getProbes() {
        return new int[] { idGenerator.nextId(), idGenerator.nextId() };
    }

    /**
     * Tells if the opcodes is of the if_icmp{@literal <cond>} family.
     *
     * @param opcode the opcode
     * @return <code>true</code> if int compare jump, else <code>false</code>.
     */
    private boolean isIF_ICMPxx(final int opcode) {
        switch (opcode) {
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
                return true;
        }

        return false;
    }

    private int jumpPopCount(final int opcode) {
        switch (opcode) {
            case Opcodes.GOTO:
                return 0;
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                return 1;
            default: // IF_CMPxx and IF_ACMPxx
                return 2;
        }
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
                                      final Label[] labels) {
        if (markLabels(dflt, labels)) {
            probesVisitor.visitLookupSwitchInsnWithProbes(dflt, keys, labels,
                    frame(1));
        } else {
            probesVisitor.visitLookupSwitchInsn(dflt, keys, labels);
        }
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
                                     final Label dflt, final Label... labels) {
        if (markLabels(dflt, labels)) {
            probesVisitor.visitTableSwitchInsnWithProbes(min, max, dflt,
                    labels, frame(1));
        } else {
            probesVisitor.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }

    private boolean isBranchCompareZero(int opcode) {
        switch (opcode) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFLE:
            case Opcodes.IFGT:
            case Opcodes.IFGE:
                return true;
        }

        return false;
    }

    private boolean markLabels(final Label dflt, final Label[] labels) {
        boolean probe = false;
        LabelInfo.resetDone(labels);
        if (LabelInfo.isMultiTarget(dflt)) {
            LabelInfo.setProbeId(dflt, idGenerator.nextId());
            probe = true;
        }
        LabelInfo.setDone(dflt);
        for (final Label l : labels) {
            if (LabelInfo.isMultiTarget(l) && !LabelInfo.isDone(l)) {
                LabelInfo.setProbeId(l, idGenerator.nextId());
                probe = true;
            }
            LabelInfo.setDone(l);
        }
        return probe;
    }

    private IFrame frame(final int popCount) {
        return FrameSnapshot.create(analyzer, popCount);
    }

}
