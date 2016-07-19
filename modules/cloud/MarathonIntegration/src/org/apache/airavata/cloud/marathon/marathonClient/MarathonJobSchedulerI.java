package org.apache.airavata.cloud.marathon.marathonClient;

import org.apache.airavata.cloud.marathon.exception.MarathonException;

public interface MarathonJobSchedulerI {

	public void jobKill(String kill, String address) throws MarathonException;
	public void jobLaunch(String name, String address) throws MarathonException;
	public void configCreate(String name, String ram, String cpu, String disk, String image, String command) throws MarathonException;
}
