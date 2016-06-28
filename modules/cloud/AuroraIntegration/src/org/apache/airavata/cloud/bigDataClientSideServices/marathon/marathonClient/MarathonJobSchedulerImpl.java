package org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;
import org.apache.airavata.cloud.utilities.marathonUtilities.MarathonUtilImpl;
import org.apache.airavata.cloud.utilities.marathonUtilities.MarathonUtilI;

public class MarathonJobSchedulerImpl implements MarathonJobSchedulerI {
	MarathonUtilI util = new MarathonUtilImpl();


	public void marathonJobCommand(String info, String command) throws MarathonException{
		try{

				//TODO:Command inmplementation
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while passing the command.\n"+ex.toString());
		}
	}

	//TODO:Rest of the commands inmplementation goes here

}
