package org.apache.airavata.cloud.aurora.auroraClient;

import org.apache.airavata.cloud.aurora.exception.AuroraException;

public interface AuroraJobSchedulerI {

	public void jobUpdateInfo(String info) throws AuroraException;
	public void jobUpdate(String update) throws AuroraException;
	public void jobUpdateResume(String info) throws AuroraException;
	public void jobUpdateAbort(String info) throws AuroraException;
	public void jobUpdateList(String info) throws AuroraException;
	public void jobUpdatePause(String info) throws AuroraException;
	public void auroraJobCommand(String info, String command) throws AuroraException;
	public void jobRestart(String restart) throws AuroraException;
	public void jobKill(String kill) throws AuroraException;
	public void jobLaunch(String name) throws AuroraException;
	public void jobDiff(String key, String config) throws AuroraException;
	public void jobInspect(String key, String config) throws AuroraException;
	public void clusterQuota(String key) throws AuroraException;
	public void configList(String config) throws AuroraException;
	public void openWebUI(String key) throws AuroraException;
	public void configCreate(String name, String ram, String cpu, String disk, String image) throws AuroraException;
}
