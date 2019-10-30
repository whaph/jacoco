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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Boundary {

    private final int line;
    private final List<Check> checks;

    public Boundary(int line, List<Check> checks) {
        this.line = line;
        this.checks = checks;
    }

    public int getLine() {
        return line;
    }

    public List<Check> getChecks() {
        return checks;
    }

    public ICounter getCounter() {
        int covered = 0;
        for (Check check : checks) {
            if (check.isCovered()) {
                covered++;
            }
        }

        int missed = checks.size() - covered;
        return CounterImpl.getInstance(missed, covered);
    }

    enum Kind {
        LEFT,
        MIDDLE,
        RIGHT
    }

    public static final class Check {
        private final boolean covered;
        private final Kind kind;

        public Check(boolean covered, Kind kind) {
            this.covered = covered;
            this.kind = kind;
        }

        public boolean isCovered() {
            return covered;
        }

        public Kind getKind() {
            return kind;
        }
    }

    Boundary merge(Boundary other) {
        if (line != other.line) {
            throw new IllegalArgumentException();
        }

        if (checks.size() != other.checks.size()) {
            throw new IllegalArgumentException();
        }

        List<Check> newChecks = new ArrayList<Check>(checks.size());
        for (int i = 0; i < checks.size(); i++) {
            Check check = checks.get(i);
            Check otherCheck = other.checks.get(i);
            if (check.getKind() != otherCheck.getKind()) {
                throw new IllegalArgumentException();
            }

            Check newCheck = new Check(check.covered || otherCheck.covered, check.getKind());
            newChecks.set(i, newCheck);
        }

        return new Boundary(line, newChecks);
    }
}
