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
	public void deleteMarathonLeader(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X DELETE "+address+"/v2/leader");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the launch queue.\n"+ex.toString());
		}
	}
	public void marathonLeader(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/leader");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the leader information.\n"+ex.toString());
		}
	}
	public void marathonInfo(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/info");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the marathon information.\n"+ex.toString());
		}
	}
	public void launchQueue(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/queue");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the launch queue.\n"+ex.toString());
		}
	}
	public void eventSubscriptionList(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/eventSubscriptions");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list of event subscriptions.\n"+ex.toString());
		}
	}
	public void eventsList(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/events");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list of events.\n"+ex.toString());
		}
	}
	public void deleteDeployment(String address, String id) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X DELETE "+address+"/v2/deployments/"+id);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while deleting the deployment.\n"+ex.toString());
		}
	}
	public void deploymentList(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/deployments");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list of deployment.\n"+ex.toString());
		}
	}
	public void deleteGroups(String address, String id) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X DELETE "+address+"/v2/groups/"+id);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while deleting the group.\n"+ex.toString());
		}
	}
	public void createGroups(String address, String json) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl -X POST -H \"Content-type: application/json\" "+address+"/v2/groups/"+json);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while creating the group.\n"+ex.toString());
		}
	}
	public void groups(String address) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/groups/");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list of groups.\n"+ex.toString());
		}
	}
	public void groupsId(String address, String groupid) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/groups/"+groupid);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list of groups.\n"+ex.toString());
		}
	}
	public void jobDeleteId(String address, String appid, String taskid) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl DELETE "+address+"/v2/apps/"+appid+"/"+taskid);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
	public void jobDelete(String address, String appid) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl DELETE "+address+"/v2/apps/"+appid+"/tasks");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
	public void runningJobs(String address, String appid) throws MarathonException{
		try{
			String line;
			Process marathonJob = Runtime.getRuntime().exec("curl GET "+address+"/v2/apps/"+appid+"/tasks");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(marathonJob.getInputStream()));
			util.printLog(stdout);
			marathonJob.waitFor();
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while retrieving the list jobs.\n"+ex.toString());
		}
	}
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
		String config = "'id': "+name+",'cmd': \""+command+"\", \"container\": {\"type\": \"DOCKER\", \"docker\": {\"image\": \""+image+"\", \"forcePullImage\": bool(1)}},\"constraints\":[[\"hostname\",\"UNIQUE\"]],\"cpus\": float("+cpu+"), \"mem\": "+ram+"), \"disk\": "+disk+", \"instances\": 1";
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
