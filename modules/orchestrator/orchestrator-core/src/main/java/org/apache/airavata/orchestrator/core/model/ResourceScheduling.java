package org.apache.airavata.orchestrator.core.model;

public class ResourceScheduling {
	
	private boolean autoSchedule;
	private boolean overrideManualScheduledParams;
	private String hostName;
	private int cpuCount;
	private int nodeCount;
	private String queueName;
	private int maxWallTime;
	
	public boolean isAutoSchedule() {
		return autoSchedule;
	}
	public void setAutoSchedule(boolean autoSchedule) {
		this.autoSchedule = autoSchedule;
	}
	public boolean isOverrideManualScheduledParams() {
		return overrideManualScheduledParams;
	}
	public void setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
		this.overrideManualScheduledParams = overrideManualScheduledParams;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getCpuCount() {
		return cpuCount;
	}
	public void setCpuCount(int cpuCount) {
		this.cpuCount = cpuCount;
	}
	public int getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public int getMaxWallTime() {
		return maxWallTime;
	}
	public void setMaxWallTime(int maxWallTime) {
		this.maxWallTime = maxWallTime;
	}
	
}
