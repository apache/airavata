package org.apache.airavata.cloud.aurora.auroraClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.airavata.cloud.aurora.exception.AuroraException;
import org.apache.airavata.cloud.aurora.utilities.AuroraUtilImpl;
import org.apache.airavata.cloud.aurora.utilities.AuroraUtilI;

public class AuroraJobSchedulerImpl implements AuroraJobSchedulerI {
	AuroraUtilI util = new AuroraUtilImpl();
	public void auroraJobCommand(String info, String command) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora task run example/benchmarks/devel/"+info+" "+command);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {
			throw new AuroraException("Exception occured while passing the command.\n"+ex.toString());
		}
	}
	public void jobUpdateList(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update list example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while listing the update.\n"+ex.toString());
		}
	}
	public void jobUpdateAbort(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora abort pause example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while aborting the update.\n"+ex.toString());
		}
	}
	public void jobUpdateResume(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update resume example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while resuming the update.\n"+ex.toString());
		}
	}
	public void jobUpdatePause(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update pause example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while pausing the update.\n"+ex.toString());
		}
	}
	public void jobUpdateInfo(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update info example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));


			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while retrieving the update info."+ex.toString());
		}
	}

	public void jobUpdate(String update) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update start example/benchmarks/devel/"+update+" "+update+".aurora");
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));


			util.printLog(stdout);
		}

		catch (Exception ex) {

			throw new AuroraException("Exception occured while updating the job.\n"+ex.toString());
   		}
	}
	public void jobRestart(String restart) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job restart example/benchmarks/devel/"+restart);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
		}

		catch (Exception ex) {
			throw new AuroraException("Exception occured while restarting the job.\n"+ex.toString());
   		}
	}

	public void jobKill(String kill) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job killall example/benchmarks/devel/"+kill);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));


			util.printLog(stdout);
		}

		catch (Exception ex) {
			throw new AuroraException("Exception occured while killing the job.\n"+ex.toString());
		}
	}
	public void jobLaunch(String name) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job create example/benchmarks/devel/"+name+" "+name+".aurora");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));

			util.printLog(stdout);
			auroraJob.waitFor();
		}

		catch (Exception ex) {
			throw new AuroraException("Exception occured while launching the job.\n"+ex.toString());
		}
	}
	public void configCreate(String name, String ram, String cpu, String disk, String image) throws AuroraException{
	try {
		String config = "import hashlib\n"+name+"= Process(name = '"+name+"', cmdline = 'java -jar /dacapo-9.12-bach.jar "+name+" -s small')\n"+name+"_task = SequentialTask(processes = [ "+name+"], resources = Resources(cpu = "+cpu+", ram = "+ram+"*MB, disk="+disk+"*MB))\njobs = [ Job(cluster = 'example', environment = 'devel', role = 'benchmarks', name = '"+name+"', task = "+name+"_task, instances =1 , container = Container(docker = Docker(image = '"+image+"')))]\n";
		File file = new File(name+".aurora");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(config);
		bw.close();

		}catch (IOException ex) {
			throw new AuroraException("IO Exception occured while creating the configuration file.\n"+ex.toString());
		}catch (Exception ex) {
			throw new AuroraException("Exception occured while creating the configuration file.\n"+ex.toString());
		}
	}
}
