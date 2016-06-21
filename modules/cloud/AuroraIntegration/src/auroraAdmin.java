import java.io.*;
import java.util.*;

public class auroraAdmin{
	public static void main(String[] args) {
		auroraJobScheduler auroraJS = new auroraJobScheduler();
		final Map<String, List<String>> params = new HashMap<>();
		List<String> options = null;
		for (int i = 0; i < args.length; i++) {
    			final String a = args[i];
		    	if (a.charAt(0) == '-') {
			        if (a.length() < 2) {
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
        			System.err.println("Illegal parameter usage");
        			return;
    			}
		}
		String RamSize,JobName,CpuCount,DiskSize,Option,Image;
		Option = params.get("o").get(0);
		if (Option.equals("kill")){
			try{
				auroraJS.jobKill(params.get("n").get(0));
			}catch(AuroraException ex){
			}						
		}
                else if (Option.equals("restart")){
			try {
				auroraJS.jobRestart(params.get("n").get(0));
			} catch (AuroraException e) {}
		}
		else if (Option.equals("update")){
			try{
				auroraJS.jobUpdate(params.get("n").get(0));
			}catch(AuroraException ex){}
		}
		else if (Option.equals("update-info")){
			try{
				auroraJS.jobUpdateInfo(params.get("n").get(0));
			}catch(AuroraException ex){}
		}
		else if (Option.equals("create")){
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
			} catch (AuroraException e) {}
		}
	}
}
