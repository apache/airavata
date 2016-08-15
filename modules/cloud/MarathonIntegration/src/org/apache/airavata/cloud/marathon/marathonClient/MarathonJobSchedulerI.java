package org.apache.airavata.cloud.marathon.marathonClient;

import org.apache.airavata.cloud.marathon.exception.MarathonException;

public interface MarathonJobSchedulerI {

	public void deleteMarathonLeader(String address) throws MarathonException;
	public void marathonLeader(String address) throws MarathonException;
	public void marathonInfo(String address) throws MarathonException;
	public void launchQueue(String address) throws MarathonException;
	public void eventSubscriptionList(String address) throws MarathonException;
	public void eventsList(String address) throws MarathonException;
	public void deleteDeployment(String address, String id) throws MarathonException;
	public void deploymentList(String address) throws MarathonException;
	public void deleteGroups(String address, String id) throws MarathonException;
	public void createGroups(String address, String json) throws MarathonException;
	public void groups(String address) throws MarathonException;
	public void groupsId(String address, String groupid) throws MarathonException;
	public void jobDeleteId(String address, String appid, String taskid) throws MarathonException;
	public void jobDelete(String address, String appid) throws MarathonException;
	public void runningJobs(String address, String appid) throws MarathonException;
	public void jobListById(String address, String id) throws MarathonException;
	public void jobListByName(String address, String name) throws MarathonException;
	public void jobList(String address) throws MarathonException;
	public void jobKill(String kill, String address) throws MarathonException;
	public void jobLaunch(String name, String address) throws MarathonException;
	public void configCreate(String name, String ram, String cpu, String disk, String image, String command) throws MarathonException;
}
