package org.jacoco.examples.boundary; /*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *
 *******************************************************************************/

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Example to run coverage analysis including boundary value coverage.
 *
 * @author Aykut Yilmaz
 */
public class ExampleRunner {
    /**
     * A class loader that loads classes from in-memory data.
     *
     * @see org.jacoco.examples.CoreTutorial.MemoryClassLoader Original
     */
    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();

        /**
         * Add a in-memory representation of a class.
         *
         * @param name
         *            name of the class
         * @param bytes
         *            class definition
         */
        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }

    }

    private final PrintStream out;

    /**
     * Creates a new example instance printing to the given stream.
     *
     * @param out
     *            stream for outputs
     */
    public ExampleRunner(final PrintStream out) {
        this.out = out;
    }

    /**
     * Run this example.
     *
     * @throws Exception
     *             in case of errors
     */
    public void execute(String destination, String source, Class<?>... classes) throws Exception {

        // For instrumentation and runtime we need a IRuntime instance
        // to collect execution data:
        final IRuntime runtime = new LoggerRuntime();

        // The Instrumenter creates a modified version of our test target class
        // that contains additional probes for execution data recording:
        final Instrumenter instr = new Instrumenter(runtime);
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();

        for (Class<?> clazz: classes) {
            final String targetName = clazz.getName();
            InputStream original = getTargetClass(targetName);
            final byte[] instrumented = instr.instrument(original, targetName);
            original.close();

            // Now we're ready to run our instrumented class and need to startup the
            // runtime first:
            final RuntimeData data = new RuntimeData();
            runtime.startup(data);


            // In this tutorial we use a special class loader to directly load the
            // instrumented class definition from a byte[] instances.
            final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
            memoryClassLoader.addDefinition(targetName, instrumented);
            final Class<?> targetClass = memoryClassLoader.loadClass(targetName);
            // Here we execute our test target class through its Runnable interface:
            final Runnable targetInstance = (Runnable) targetClass.newInstance();
            targetInstance.run();

            // At the end of test execution we collect execution data and shutdown
            // the runtime:

            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            // Together with the original class definition we can calculate coverage
            // information:

            final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
            original = getTargetClass(targetName);
            analyzer.analyzeClass(original, targetName);
            original.close();
        }

        final IBundleCoverage node = coverageBuilder.getBundle("test report");
        createReport(node, executionData, sessionInfos, destination, source);
    }

    private void createReport(final IBundleCoverage bundleCoverage, ExecutionDataStore executionData, SessionInfoStore sessionInfos, String destination, String source)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final File baseDir = new File(destination);
        baseDir.mkdirs();
        final IReportVisitor visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(baseDir));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(sessionInfos.getInfos(),
                executionData.getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
                new File(source), "utf-8", 4));

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private InputStream getTargetClass(final String name) {
        final String resource = '/' + name.replace('.', '/') + ".class";
        return getClass().getResourceAsStream(resource);
    }

    private void execute(String destination, String source) throws Exception {
        execute(destination, source, IntExample.class, LongExample.class, BooleanExample.class, ByteExample.class, ShortExample.class);
    }

    /**
     * Entry point to run this Java application.
     *
     * @param args
     *            list of program arguments
     * @throws Exception
     *             in case of errors
     */
    public static void main(final String[] args) throws Exception {
        final String destination = args[0]; // destination folder for the report, e.g.: "C:\\Users\\ayilmaz\\Documents\\jacoco\\reports\\";
        final String source = args[1]; // root folder of the source code, e.g.: "C:\\Users\\ayilmaz\\Documents\\git\\BA\\bv-jacoco\\jacoco\\org.jacoco.examples\\src\\";
        new ExampleRunner(System.out).execute(destination, source);
    }
}
