package org.apache.airavata.gfac.provider.utils;



public interface ResourceRequirement extends Cloneable {
	
	/**
	 * States whether this resource requirement is active
	 * and should be written into the job description.
	 * @return
	 */
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
}
