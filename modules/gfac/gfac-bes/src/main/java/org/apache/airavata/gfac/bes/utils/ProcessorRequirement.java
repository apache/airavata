/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.gfac.bes.utils;

public enum ProcessorRequirement{
	sparc("sparc"), //$NON-NLS-1$
	powerpc("powerpc"), //$NON-NLS-1$
	x86("x86"), //$NON-NLS-1$
	x86_32("x86_32"), //$NON-NLS-1$
	x86_64("x86_64"), //$NON-NLS-1$
	parisc("parisc"), //$NON-NLS-1$
	mips("mips"), //$NON-NLS-1$
	ia64("ia64"), //$NON-NLS-1$
	arm("arm"), //$NON-NLS-1$
	other("other"); //$NON-NLS-1$

	ProcessorRequirement(String value) {
		this.value = value; 
	}

	private final String value;

	public String getValue() { 
		return value; 
	}

	public static ProcessorRequirement fromString(String value)
	{
		for (ProcessorRequirement type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		return other;
	}
	
	public String toString()
	{
		return value;
	}

}
