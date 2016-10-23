package org.apache.airavata.cloud.aurora.client.bean;

/**
 * The Class ResourceBean.
 */
public class ResourceBean {
	
	/** The num cpus. */
	private double numCpus;
	
	/** The disk mb. */
	private long diskMb;
	
	/** The ram mb. */
	private long ramMb;
	
	/**
	 * Instantiates a new resource bean.
	 *
	 * @param numCpus the num cpus
	 * @param diskMb the disk mb
	 * @param ramMb the ram mb
	 */
	public ResourceBean(double numCpus, long diskMb, long ramMb) {
		this.numCpus = numCpus;
		this.diskMb = diskMb;
		this.ramMb = ramMb;
	}

	/**
	 * Gets the num cpus.
	 *
	 * @return the num cpus
	 */
	public double getNumCpus() {
		return numCpus;
	}

	/**
	 * Sets the num cpus.
	 *
	 * @param numCpus the new num cpus
	 */
	public void setNumCpus(double numCpus) {
		this.numCpus = numCpus;
	}

	/**
	 * Gets the disk mb.
	 *
	 * @return the disk mb
	 */
	public long getDiskMb() {
		return diskMb;
	}

	/**
	 * Sets the disk mb.
	 *
	 * @param diskMb the new disk mb
	 */
	public void setDiskMb(long diskMb) {
		this.diskMb = diskMb;
	}

	/**
	 * Gets the ram mb.
	 *
	 * @return the ram mb
	 */
	public long getRamMb() {
		return ramMb;
	}

	/**
	 * Sets the ram mb.
	 *
	 * @param ramMb the new ram mb
	 */
	public void setRamMb(long ramMb) {
		this.ramMb = ramMb;
	}

	
}
