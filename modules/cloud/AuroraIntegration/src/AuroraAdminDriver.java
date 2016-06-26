
// rename class to AuroraAdminDriver
// TOOD: need javadoc documentation at the top of each method

import java.util.*;

import exception.AuroraException;

public class AuroraAdminDriver{
	public static void main(String[] args) {

	    // TODO: add code to call a method to validate the args

	    // TODO: program to an interface. So, the code should be 
	    // AuroraJobSchedulerI auroraJS = new AuroraJobSchedulerImpl();
	    
		AuroraJobScheduler auroraJS = new AuroraJobScheduler();
		// why is htis Map final? : Does not have to be final, all the implementations of argument readers i saw had final for Map
		
		// documentation: what is the purpose of the for-loop : to read all the arguments for example to create a job following are the arguments: -o create -r 1024 -n batik -c 2.0 -d 1000 -i gouravr/dacapo:tag9
  
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
			    // TOOD: need an example of correct parameter usage
        			System.err.println("Illegal parameter usage\nOptions:\n1) -o\tcreate, kill, restart, update, update-info, update-pause\n2) -n\tname of the job\n 3) -r\tamount of RAM\n 4) -c\tCPU count\n 5) -d\tdisk space\n 6) -k\tname of the task to be killed\n 7) -i\texecutable/image\n ");
        			return;
    			}
		}
		// These Strings should be Enums
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
