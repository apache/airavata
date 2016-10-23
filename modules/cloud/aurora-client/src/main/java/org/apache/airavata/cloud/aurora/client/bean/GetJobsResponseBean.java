package org.apache.airavata.cloud.aurora.client.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class GetJobsResponseBean.
 */
public class GetJobsResponseBean extends ResponseBean {

	/** The job configs. */
	private Set<JobConfigBean> jobConfigs;

	/**
	 * Gets the job configs.
	 *
	 * @return the job configs
	 */
	public Set<JobConfigBean> getJobConfigs() {
		if(jobConfigs == null) {
			jobConfigs = new HashSet<>();
		}
		return jobConfigs;
	}

	/**
	 * Sets the job configs.
	 *
	 * @param jobConfigs the new job configs
	 */
	public void setJobConfigs(Set<JobConfigBean> jobConfigs) {
		this.jobConfigs = jobConfigs;
	}
}
