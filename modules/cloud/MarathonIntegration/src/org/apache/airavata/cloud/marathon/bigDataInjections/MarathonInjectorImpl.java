
package org.apache.airavata.cloud.marathon.bigDataInjections;

import java.util.Map;
import java.util.List;


import org.apache.airavata.cloud.marathon.marathonClient.MarathonJobSchedulerI;
import org.apache.airavata.cloud.marathon.marathonClient.MarathonJobSchedulerImpl;
import org.apache.airavata.cloud.marathon.exception.MarathonException;


public class MarathonInjectorImpl implements BigDataInjectorI {
    private MarathonJobSchedulerI marathonJS = null;
    
    public MarathonInjectorImpl(MarathonJobSchedulerI marathonJSIn) {
	marathonJS = marathonJSIn;
    }


    public void executeTheBigDataClientSideCommand(Map<String, List<String>> commandLineOptions) {

	
	String commandName = commandLineOptions.get("o").get(0);
	String RamSize, JobName, CpuCount, DiskSize, Image, Command;

		switch(commandName)
		{
			case "kill" :
				try {
					marathonJS.jobKill(commandLineOptions.get("n").get(0),commandLineOptions.get("a").get(0));
				} catch(MarathonException ex){
				} break;
			case "create" :
				JobName = commandLineOptions.get("n").get(0);
				RamSize = commandLineOptions.get("r").get(0);
				CpuCount = commandLineOptions.get("c").get(0);
				DiskSize = commandLineOptions.get("d").get(0);
				Image = commandLineOptions.get("i").get(0);
				Command = commandLineOptions.get("a").get(0);
				try {
					marathonJS.configCreate(JobName,RamSize,CpuCount,DiskSize,Image, Command);
				} catch (MarathonException ex) {}
		 		try {
					marathonJS.jobLaunch(JobName,commandLineOptions.get("a").get(0));
				} catch (MarathonException ex) {
				} break;
			default :
				System.out.println("Improper option\nOptions available:\n1) create\n2) kill\n");
		}
    } // end of public void executeTheBigDataCommand
} // end of public class AuroraInjectorImpl
