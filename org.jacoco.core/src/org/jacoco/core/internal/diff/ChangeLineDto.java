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
package org.jacoco.core.internal.diff;

public class ChangeLineDto {
	private String type;
	private Integer startLineNum;
	private Integer endLineNum;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getStartLineNum() {
		return this.startLineNum;
	}

	public void setStartLineNum(Integer startLineNum) {
		this.startLineNum = startLineNum;
	}

	public Integer getEndLineNum() {
		return this.endLineNum;
	}

	public void setEndLineNum(Integer endLineNum) {
		this.endLineNum = endLineNum;
	}
}
