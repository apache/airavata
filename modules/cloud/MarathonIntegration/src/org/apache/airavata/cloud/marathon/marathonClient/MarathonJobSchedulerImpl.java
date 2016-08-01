package org.apache.airavata.cloud.marathon.marathonClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.airavata.cloud.marathon.exception.MarathonException;
import org.apache.airavata.cloud.marathon.utilities.MarathonUtilImpl;
import org.apache.airavata.cloud.marathon.utilities.MarathonUtilI;

public class MarathonJobSchedulerImpl implements MarathonJobSchedulerI {
	MarathonUtilI util = new MarathonUtilImpl();
	public void jobListById(String address, String id) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/apps/"+id);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
	public void jobListByName(String address, String name) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/apps/"+name);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
	public void jobList(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/apps");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
	public void jobKill(String name, String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X DELETE "+address+""+name);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while killing the job.\n"+ex.toString());
		}
	}
	public void jobLaunch(String name, String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X POST "+address+"/v2/apps -d @"+name+" -H Content-type: application/json");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while launching the job.\n"+ex.toString());
		}
	}
	public void configCreate(String name, String ram, String cpu, String disk, String image, String command) throws MarathonException{
	try {
		String config = "'id': "+name+",'cmd': \""+command+"\", \"container\": {\"type\": \"DOCKER\", \"docker\": {\"image\": \"danielpan/dacapo\", \"forcePullImage\": bool(1)}},\"constraints\":[[\"hostname\",\"UNIQUE\"]],\"cpus\": float("+cpu+"), \"mem\": "+ram+"), \"disk\": "+disk+", \"instances\": 1";
		File file = new File(name+".json");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(config);
		bw.close();

		}catch (IOException ex) {
			throw new MarathonException("IO Exception occured while creating the configuration file.\n"+ex.toString());
		}catch (Exception ex) {
			throw new MarathonException("Exception occured while creating the configuration file.\n"+ex.toString());
		}
	}
}
