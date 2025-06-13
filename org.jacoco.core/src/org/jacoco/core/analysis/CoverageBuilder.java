/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.analysis;

import java.util.*;

import org.jacoco.core.internal.analysis.BundleCoverageImpl;
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl;
import org.jacoco.core.internal.diff.ChangeLineDto;
import org.jacoco.core.internal.diff.ClassInfoDto;
import org.jacoco.core.internal.diff.CodeDiffUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Builder for hierarchical {@link ICoverageNode} structures from single
 * {@link IClassCoverage} nodes. The nodes are feed into the builder through its
 * {@link ICoverageVisitor} interface. Afterwards the aggregated data can be
 * obtained with {@link #getClasses()}, {@link #getSourceFiles()} or
 * {@link #getBundle(String)} in the following hierarchy:
 *
 * <pre>
 * {@link IBundleCoverage}
 * +-- {@link IPackageCoverage}*
 *     +-- {@link IClassCoverage}*
 *     +-- {@link ISourceFileCoverage}*
 * </pre>
 */
public class CoverageBuilder implements ICoverageVisitor {

	private final Map<String, IClassCoverage> classes;

	private final Map<String, ISourceFileCoverage> sourcefiles;

	/**
	 * 新增代码类
	 */
	public List<ClassInfoDto> classInfos;

	/**
	 * Create a new builder.
	 */
	public CoverageBuilder() {
		this.classes = new HashMap<String, IClassCoverage>();
		this.sourcefiles = new HashMap<String, ISourceFileCoverage>();
	}

	public CoverageBuilder(String classList) {
		this.classes = new HashMap<String, IClassCoverage>();
		this.sourcefiles = new HashMap<String, ISourceFileCoverage>();
		if (null != classList && !"".equals(classList)) {
			Gson gson = new Gson();
			classInfos = gson.fromJson(classList,
					new TypeToken<List<ClassInfoDto>>() {
					}.getType());
		}
	}

	public List<ClassInfoDto> getClassInfos() {
		return classInfos;
	}

	public void setClassInfos(List<ClassInfoDto> classInfos) {
		this.classInfos = classInfos;
	}

	/**
	 * Returns all class nodes currently contained in this builder.
	 *
	 * @return all class nodes
	 */
	public Collection<IClassCoverage> getClasses() {
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Returns all source file nodes currently contained in this builder.
	 *
	 * @return all source file nodes
	 */
	public Collection<ISourceFileCoverage> getSourceFiles() {
		return Collections.unmodifiableCollection(sourcefiles.values());
	}

	/**
	 * Creates a bundle from all nodes currently contained in this bundle.
	 *
	 * @param name
	 *            Name of the bundle
	 * @return bundle containing all classes and source files
	 */
	public IBundleCoverage getBundle(final String name) {
		return new BundleCoverageImpl(name, classes.values(),
				sourcefiles.values());
	}

	/**
	 * Returns all classes for which execution data does not match.
	 *
	 * @return collection of classes with non-matching execution data
	 * @see IClassCoverage#isNoMatch()
	 */
	public Collection<IClassCoverage> getNoMatchClasses() {
		final Collection<IClassCoverage> result = new ArrayList<IClassCoverage>();
		for (final IClassCoverage c : classes.values()) {
			if (c.isNoMatch()) {
				result.add(c);
			}
		}
		return result;
	}

	// === ICoverageVisitor ===

	public void visitCoverage(final IClassCoverage coverage) {
		final String name = coverage.getName();
		final IClassCoverage dup = classes.put(name, coverage);
		if (dup != null) {
			if (dup.getId() != coverage.getId()) {
				throw new IllegalStateException(
						"Can't add different class with same name: " + name);
			}
		} else {
			final String source = coverage.getSourceFileName();
			if (source != null) {
				final SourceFileCoverageImpl sourceFile = getSourceFile(source,
						coverage.getPackageName());
				sourceFile.increment(coverage);

				Boolean changeFlag = CodeDiffUtil
						.checkClassIn(coverage.getName(), this.classInfos);
				if (changeFlag.booleanValue()) {
					ClassInfoDto classInfoDto = this.classInfos.stream().filter(
							c -> (coverage.getName().equals(c.getClassFile())
									|| coverage.getName().split("\\$")[0]
											.equals(c.getClassFile())))
							.findFirst().orElse(null);
					if (null == classInfoDto) {
						return;
					}

					// 行信息注入到SourceFile
					List<ChangeLineDto> lines = classInfoDto.getLines();
					if (null == lines || lines.isEmpty()
							|| coverage.getFirstLine() == -1) {
						return;
					}
					sourceFile.setChangeLinesInfo(lines);

				}
			}
		}
	}

	private SourceFileCoverageImpl getSourceFile(final String filename,
			final String packagename) {
		final String key = packagename + '/' + filename;
		SourceFileCoverageImpl sourcefile = (SourceFileCoverageImpl) sourcefiles
				.get(key);
		if (sourcefile == null) {
			sourcefile = new SourceFileCoverageImpl(filename, packagename);
			sourcefiles.put(key, sourcefile);
		}
		return sourcefile;
	}

}
