package org.apache.airavata.gfac.provider.utils;

/**
 * @version $Id: ProcessorType.java,v 1.2 2006/07/25 10:43:39 baklushin Exp $
 * @author Alexei Baklushin
 */
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
