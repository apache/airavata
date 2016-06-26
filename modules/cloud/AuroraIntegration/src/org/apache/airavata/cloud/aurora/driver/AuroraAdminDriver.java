package org.apache.airavata.cloud.aurora.driver;
// TODO: need javadoc documentation at the top of each method

// TODO: individually import the types
import java.util.*;

import org.apache.airavata.cloud.aurora.exception.AuroraException;
import org.apache.airavata.cloud.aurora.auroraClient.AuroraJobSchedulerI;
import org.apache.airavata.cloud.aurora.auroraClient.AuroraJobSchedulerImpl;

public class AuroraAdminDriver{
	public static void main(String[] args) {

		AuroraJobSchedulerI auroraJS = new AuroraJobSchedulerImpl();

		Map<String, List<String>> params = new HashMap<>();
		List<String> options = null;
		for (int i = 0; i < args.length; i++) {
    			final String a = args[i];
		    	if (a.charAt(0) == '-') {
			        if (a.length() < 2) {
				    // TOOD: need more details in the error statement
			            System.err.println("Error at argument " + a);
				    return;
        			}
			        options = new ArrayList<>();
        			params.put(a.substring(1), options);
    			}
    			else if (options != null) {
        			options.add(a);
    			}
    			else {

        			System.err.println("Illegal parameter \n[USAGE]\nOptions:\n1) -o\tcreate, kill, restart, update, update-info, update-pause\n2) -n\tname of the job\n 3) -r\tamount of RAM\n 4) -c\tCPU count\n 5) -d\tdisk space\n 6) -k\tname of the task to be killed\n 7) -i\texecutable/image\n ");
        			return;
    			}
		}

		String RamSize,JobName,CpuCount,DiskSize,Option,Image;
		Option = params.get("o").get(0);
		switch(Option)
		{
			case "kill" :
				try{
					auroraJS.jobKill(params.get("n").get(0));
				}catch(AuroraException ex){
				}break;
			case "restart" :
				try {
					auroraJS.jobRestart(params.get("n").get(0));
				} catch (AuroraException ex) {
				}break;
			case "update" :
				try{
					auroraJS.jobUpdate(params.get("n").get(0));
				}catch(AuroraException ex){
				}break;
			case "update-info" :
				try{
					auroraJS.jobUpdateInfo(params.get("n").get(0));
				}catch(AuroraException ex){
				}break;
			case "update-pause" :
				try{
					auroraJS.jobUpdatePause(params.get("n").get(0));
				}catch(AuroraException ex){
				}break;
			case "create" :
				JobName = params.get("n").get(0);
				RamSize = params.get("r").get(0);
				CpuCount = params.get("c").get(0);
				DiskSize = params.get("d").get(0);
				Image = params.get("i").get(0);
				try {
					auroraJS.configCreate(JobName,RamSize,CpuCount,DiskSize,Image);
				} catch (AuroraException ex) {}
		 		try {
					auroraJS.jobLaunch(JobName);
				} catch (AuroraException ex) {
				}break;
			default :
				System.out.println("Improper option\nOptions available:\n1) create\n2) kill\n3) restart\n4) update\n 5) update-info\n6) update-pause\n");
		}
	}
}
