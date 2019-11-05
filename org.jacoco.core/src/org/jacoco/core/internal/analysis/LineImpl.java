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
package org.jacoco.core.internal.analysis;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

/**
 * Implementation of {@link ILine}.
 */
public abstract class LineImpl implements ILine {

	/** Max instruction counter value for which singletons are created */
	private static final int SINGLETON_INS_LIMIT = 8;

	/** Max branch counter value for which singletons are created */
	private static final int SINGLETON_BRA_LIMIT = 4;

	private static final int SINGLETON_BDRY_LIMIT = 3;

	private static final LineImpl[][][][][][] SINGLETONS = new LineImpl[SINGLETON_INS_LIMIT + 1][][][][][];

	static {
		for (int i = 0; i <= SINGLETON_INS_LIMIT; i++) {
			SINGLETONS[i] = new LineImpl[SINGLETON_INS_LIMIT + 1][][][][];
			for (int j = 0; j <= SINGLETON_INS_LIMIT; j++) {
				SINGLETONS[i][j] = new LineImpl[SINGLETON_BRA_LIMIT + 1][][][];
				for (int k = 0; k <= SINGLETON_BRA_LIMIT; k++) {
					SINGLETONS[i][j][k] = new LineImpl[SINGLETON_BRA_LIMIT + 1][][];
					for (int l = 0; l <= SINGLETON_BRA_LIMIT; l++) {
                        SINGLETONS[i][j][k][l] = new LineImpl[SINGLETON_BRA_LIMIT + 1][];
                        for (int m = 0; m <= SINGLETON_BDRY_LIMIT ; m++) {
                            SINGLETONS[i][j][k][l][m] = new LineImpl[SINGLETON_BDRY_LIMIT + 1];
                            for (int n = 0; n <= SINGLETON_BDRY_LIMIT; n++) {
                                SINGLETONS[i][j][k][l][m][n] = new Fix(i, j, k, l, m, n);
                            }
                        }
					}
				}
			}
		}
	}

	/**
	 * Empty line without instructions or branches.
	 */
	public static final LineImpl EMPTY = SINGLETONS[0][0][0][0][0][0];

    private static LineImpl getInstance(final CounterImpl instructions,
                                        final CounterImpl branches,
                                        final CounterImpl boundaries) {
        final int im = instructions.getMissedCount();
        final int ic = instructions.getCoveredCount();
        final int bm = branches.getMissedCount();
        final int bc = branches.getCoveredCount();
        final int bdrym = boundaries.getMissedCount();
        final int bdryc = boundaries.getCoveredCount();
        if (im <= SINGLETON_INS_LIMIT && ic <= SINGLETON_INS_LIMIT
                && bm <= SINGLETON_BRA_LIMIT && bc <= SINGLETON_BRA_LIMIT
                && bdrym <= SINGLETON_BDRY_LIMIT && bdryc <= SINGLETON_BDRY_LIMIT) {
            return SINGLETONS[im][ic][bm][bc][bdrym][bdryc];
        }
        return new Var(instructions, branches, boundaries);
    }

	/**
	 * Mutable version.
	 */
	private static final class Var extends LineImpl {
		Var(final CounterImpl instructions, final CounterImpl branches, final CounterImpl boundaries) {
			super(instructions, branches, boundaries);
		}

		@Override
		public LineImpl increment(final ICounter instructions,
				final ICounter branches, final ICounter boundaries) {
			this.instructions = this.instructions.increment(instructions);
			this.branches = this.branches.increment(branches);
			this.boundaries = this.boundaries.increment(boundaries);
			return this;
		}
	}

	/**
	 * Immutable version.
	 */
	private static final class Fix extends LineImpl {
		public Fix(final int im, final int ic, final int bm, final int bc, final int bdrym, final int bdryc) {
			super(CounterImpl.getInstance(im, ic), CounterImpl.getInstance(bm,
					bc), CounterImpl.getInstance(bdrym, bdryc));
		}


		public LineImpl increment(final ICounter instructions,
				final ICounter branches, final ICounter boundaries) {
			return getInstance(this.instructions.increment(instructions),
					this.branches.increment(branches), this.boundaries.increment(boundaries));
		}

	}

	/** instruction counter */
	protected CounterImpl instructions;

	/** branch counter */
	protected CounterImpl branches;

	/** boundary counter */
	protected CounterImpl boundaries;

	private LineImpl(final CounterImpl instructions, final CounterImpl branches, final CounterImpl boundaries) {
		this.instructions = instructions;
		this.branches = branches;
		this.boundaries = boundaries;
	}

	/**
	 * Adds the given counters to this line.
	 * 
	 * @param instructions
	 *            instructions to add
	 * @param branches
	 *            branches to add
	 * @return instance with new counter values
	 */
	public LineImpl increment(final ICounter instructions,
			final ICounter branches) {
		return increment(instructions, branches, CounterImpl.COUNTER_0_0);
	}

	protected abstract LineImpl increment(final ICounter instructions, final ICounter branches, final ICounter boundaries);


	// === ILine implementation ===

	public int getStatus() {
		return instructions.getStatus() | branches.getStatus();
	}

	public ICounter getInstructionCounter() {
		return instructions;
	}

	public ICounter getBranchCounter() {
		return branches;
	}

	public CounterImpl getBoundaryCounter() {
		return boundaries;
	}

	@Override
	public int hashCode() {
		return 23 * instructions.hashCode() ^ branches.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ILine) {
			final ILine that = (ILine) obj;
			return this.instructions.equals(that.getInstructionCounter())
					&& this.branches.equals(that.getBranchCounter());
		}
		return false;
	}

}
