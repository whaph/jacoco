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
package org.jacoco.report.internal.html.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

	private final Locale locale;

	private String lang;

	/**
	 * Creates a new highlighter with default settings.
	 * 
	 * @param locale
	 *            locale for tooltip rendering
	 */
	public SourceHighlighter(final Locale locale) {
		this.locale = locale;
		lang = "java";
	}

	/**
	 * Specifies the source language. This value might be used for syntax
	 * highlighting. Default is "java".
	 * 
	 * @param lang
	 *            source language identifier
	 */
	public void setLanguage(final String lang) {
		this.lang = lang;
	}

	/**
	 * Highlights the given source file.
	 * 
	 * @param parent
	 *            parent HTML element
	 * @param source
	 *            highlighting information
	 * @param contents
	 *            contents of the source file
	 * @throws IOException
	 *             problems while reading the source file or writing the output
	 */
	public void render(final HTMLElement parent, final ISourceNode source,
			final Reader contents) throws IOException {
		final HTMLElement pre = parent.pre(Styles.SOURCE + " lang-" + lang
				+ " linenums");
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		int nr = 0;
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			renderCodeLine(pre, line, source.getLine(nr), nr);
		}
	}

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			final ILine line, final int lineNr) throws IOException {
		highlight(pre, line, lineNr).text(linesrc);
		pre.text("\n");
	}

	HTMLElement highlight(final HTMLElement pre, final ILine line,
			final int lineNr) throws IOException {
		final String style;
		switch (line.getStatus()) {
		case ICounter.NOT_COVERED:
			style = Styles.NOT_COVERED;
			break;
		case ICounter.FULLY_COVERED:
			style = Styles.FULLY_COVERED;
			break;
		case ICounter.PARTLY_COVERED:
			style = Styles.PARTLY_COVERED;
			break;
		default:
			return pre;
		}

		final String boundaryCovText = getBoundaryCovText(line.getBoundaryCounter());
		final String boundaryCovStyle = getBoundaryCovStyle(line.getBoundaryCounter());

		final String lineId = "L" + Integer.toString(lineNr);
		final ICounter branches = line.getBranchCounter();
		switch (branches.getStatus()) {
		case ICounter.NOT_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_NOT_COVERED + boundaryCovStyle,
					"All %2$d branches missed." + boundaryCovText, branches);
		case ICounter.FULLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_FULLY_COVERED + boundaryCovStyle,
					"All %2$d branches covered." + boundaryCovText, branches);
		case ICounter.PARTLY_COVERED:
			return span(pre, lineId, style, Styles.BRANCH_PARTLY_COVERED + boundaryCovStyle,
					"%1$d of %2$d branches missed." + boundaryCovText, branches);
		default:
			return pre.span(style, lineId);
		}
	}

	private HTMLElement span(final HTMLElement parent, final String id,
			final String style1, final String style2, final String title,
			final ICounter branches) throws IOException {
		final HTMLElement span = parent.span(style1 + " " + style2, id);
		final Integer missed = Integer.valueOf(branches.getMissedCount());
		final Integer total = Integer.valueOf(branches.getTotalCount());
		span.attr("title", String.format(locale, title, missed, total));
		return span;
	}

	private String getBoundaryCovText(ICounter boundaryCounter) {
		if (boundaryCounter.getTotalCount() == 0) {
			return "";
		}

		switch (boundaryCounter.getStatus()) {
			case ICounter.NOT_COVERED:
				return String.format(locale, "\n\nAll %d checks for boundary missed.", boundaryCounter.getTotalCount());
			case ICounter.FULLY_COVERED:
				return String.format(locale, "\n\nAll %d checks for boundary covered.", boundaryCounter.getCoveredCount());
			case ICounter.PARTLY_COVERED:
				return String.format(locale, "\n\n%d of %d checks for boundary missed.", boundaryCounter.getMissedCount(), boundaryCounter.getTotalCount());
		}

		return "";

	}

	private String getBoundaryCovStyle(ICounter boundaryCounter) {
		if (boundaryCounter.getTotalCount() == 0) {
			return "";
		}

		switch (boundaryCounter.getStatus()) {
			case ICounter.NOT_COVERED:
				return " " + Styles.BOUNDARY_NOT_COVERED;
			case ICounter.FULLY_COVERED:
				return " " + Styles.BOUNDARY_FULLY_COVERED;
			case ICounter.PARTLY_COVERED:
				return " " + Styles.BOUNDARY_PARTLY_COVERED;
		}

		return "";
	}

}
