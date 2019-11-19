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
    private final List<Boolean> checks;

    public Boundary(int line, List<Boolean> checks) {
        this.line = line;
        this.checks = checks;
    }

    public int getLine() {
        return line;
    }

    public List<Boolean> getChecks() {
        return checks;
    }

    public ICounter getCounter() {
        int covered = 0;
        for (Boolean check : checks) {
            if (check) {
                covered++;
            }
        }

        int missed = checks.size() - covered;
        return CounterImpl.getInstance(missed, covered);
    }

    Boundary merge(Boundary other) {
        if (line != other.line) {
            throw new IllegalArgumentException();
        }

        if (checks.size() != other.checks.size()) {
            throw new IllegalArgumentException();
        }

        List<Boolean> newChecks = new ArrayList<Boolean>(checks.size());
        for (int i = 0; i < checks.size(); i++) {
            Boolean check = checks.get(i);
            Boolean otherCheck = other.checks.get(i);
            Boolean newCheck = check || otherCheck;
            newChecks.set(i, newCheck);
        }

        return new Boundary(line, newChecks);
    }
}
