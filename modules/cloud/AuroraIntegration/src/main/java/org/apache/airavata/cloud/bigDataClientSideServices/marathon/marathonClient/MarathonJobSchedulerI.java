package org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient;

import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;

public interface MarathonJobSchedulerI {

	public void marathonJobCommand(String info, String command) throws MarathonException;

}
