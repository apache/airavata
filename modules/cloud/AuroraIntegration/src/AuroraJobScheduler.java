
// TODO: add documentation on the purpose of this class

//TODO: rename this class to AuroraJobSchedulerImpl

// TODO: need javadoc style documentation for each method

//TODO: import each type individually instead of "*"
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import exception.AuroraException;

public class AuroraJobScheduler implements AuroraJobSchedulerI {
	public void jobUpdatePause(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update pause example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line); 
				line = stdout.readLine();
			}
		}
		catch (IOException ex) {
		    // should get the IOException in a string, add your own meaningful message, and then throw it again
			
			throw new AuroraException("IO Exception occured while pausing the update.\n"+ex.toString());
		}
		catch (Exception ex) {
		    // should get the Exception in a string, add your own meaningful message, and then throw it again
			throw new AuroraException("Exception occured while pausing the update.\n"+ex.toString());
		}
	}
	public void jobUpdateInfo(String info) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update info example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line); 
				line = stdout.readLine();
			}
		}
		catch (IOException ex) {
		    // should get the IOException in a string, add your own meaningful message, and then throw it again
			throw new AuroraException("IO Exception occured while retrieving the update info.\n"+ex.toString());
		}
		catch (Exception ex) {
		    // should get the Exception in a string, add your own meaningful message, and then throw it again
			throw new AuroraException("Exception occured while retrieving the update info."+ex.toString());
		}
	}

	public void jobUpdate(String update) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update start example/benchmarks/devel/"+update+" "+update+".aurora");
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line); 
				line = stdout.readLine();
			}
		}catch (IOException ex) {
		    // should get the IOException in a string, add your own meaningful message, and then throw it again
			throw new AuroraException("IO Exception occured while updating the job.\n"+ex.toString());
		}catch (Exception ex) {
		    // TODO: should get the Exception in a string, add your own meaningful message, and then throw it again
			throw new AuroraException("Exception occured while updating the job.\n"+ex.toString());
   		}
	}
	public void jobRestart(String restart) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job restart example/benchmarks/devel/"+restart);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line); 
				line = stdout.readLine();
			}
		}catch (IOException ex) {
			throw new AuroraException("IO Exception occured while restarting the job.\n"+ex.toString());
		}catch (Exception ex) {
			throw new AuroraException("Exception occured while restarting the job.\n"+ex.toString());
   		}
	}

	public void jobKill(String kill) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job killall example/benchmarks/devel/"+kill);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line); 
				line = stdout.readLine();
			}
		}catch (IOException ex) {
			throw new AuroraException("IO Exception occured while killing the job.\n"+ex.toString());
		}catch (Exception ex) {
			throw new AuroraException("Exception occured while killing the job.\n"+ex.toString());
		}
	}
	public void jobLaunch(String name) throws AuroraException{
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job create example/benchmarks/devel/"+name+" "+name+".aurora");
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			line = stdout.readLine();
			while (line != null) {
				System.out.println(line);
				line = stdout.readLine();
			}
			auroraJob.waitFor();
		}catch (IOException ex) {
			throw new AuroraException("IO Exception occured while launching the job.\n"+ex.toString());
		}catch (Exception ex) {
			throw new AuroraException("Exception occured while launching the job.\n"+ex.toString());
		}
	}
	public void configCreate(String name, String ram, String cpu, String disk, String image) throws AuroraException{
	try {
		String config = "import hashlib\n"+name+"= Process(name = '"+name+"', cmdline = 'java -jar /dacapo-9.12-bach.jar "+name+" -s small')\n"+name+"_task = SequentialTask(processes = [ "+name+"], resources = Resources(cpu = "+cpu+", ram = "+ram+"*MB, disk="+disk+"*MB))\njobs = [ Job(cluster = 'example', environment = 'devel', role = 'benchmarks', name = '"+name+"', task = "+name+"_task, instances =1 , container = Container(docker = Docker(image = '"+image+"')))]\n";
                //String line2 = name+"= Process(name = '"+name+"', cmdline = 'java -jar /dacapo-9.12-bach.jar "+name+" -s small')\n";
                //String line3 = name+"_task = SequentialTask(processes = [ "+name+"], resources = Resources(cpu = "+cpu+", ram = "+ram+"*MB, disk="+disk+"*MB))\n";
                //String line4 = "jobs = [ Job(cluster = 'example', environment = 'devel', role = 'benchmarks', name = '"+name+"', task = "+name+"_task, instances =1 , container = Container(docker = Docker(image = '"+image+"')))]\n";

		File file = new File(name+".aurora");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(config);
                //bw.write(line2);
                //bw.write(line3);
                //bw.write(line4);
		bw.close();

		}catch (IOException ex) {
			throw new AuroraException("IO Exception occured while creating the configuration file.\n"+ex.toString());
		}catch (Exception ex) {
			throw new AuroraException("Exception occured while creating the configuration file.\n"+ex.toString());
		}
	}
}			
