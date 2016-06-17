import java.io.*;
import java.util.*;

public class auroraCreate {
	public static void main(String[] args) {
		auroraCreate auroraJob = new auroraCreate();
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
		String ram,name,cpu,disk,kill,option,image;
		option = params.get("o").get(0);
		if (option.equals("kill")){
			auroraJob.jobKill(params.get("n").get(0));
		}
                else if (option.equals("restart")){
			auroraJob.jobRestart(params.get("n").get(0));
		}
		else if (option.equals("update")){
			auroraJob.jobUpdate(params.get("n").get(0));
		}
		else if (option.equals("update-info")){
			auroraJob.jobUpdateInfo(params.get("n").get(0));
		}
		else if (option.equals("create")){
			name = params.get("n").get(0);
			ram = params.get("r").get(0);
			cpu = params.get("c").get(0);
			disk = params.get("d").get(0);
			image = params.get("i").get(0);
			auroraJob.configCreate(name,ram,cpu,disk,image);
	 		auroraJob.jobLaunch(name);
		}
	}
	public void jobUpdateInfo(String info){
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update info example/benchmarks/devel/"+info);
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			while ((line = stdout.readLine()) != null) {
				System.out.println(line); 
			}
		}catch (Exception ex) {
   			ex.printStackTrace();
   		}
	}
	public void jobUpdate(String update){
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora update start example/benchmarks/devel/"+update+" "+update+".aurora");
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			while ((line = stdout.readLine()) != null) {
				System.out.println(line); 
			}
		}catch (Exception ex) {
   			ex.printStackTrace();
   		}
	}
	public void jobRestart(String restart){
		try{
			Process auroraJob = Runtime.getRuntime().exec("aurora job restart example/benchmarks/devel/"+restart);
		}catch (Exception ex) {
   			ex.printStackTrace();
   		}
	}

	public void jobKill(String kill){
		try{
			Process auroraJob = Runtime.getRuntime().exec("aurora job killall example/benchmarks/devel/"+kill);
		}catch (Exception ex) {
   			ex.printStackTrace();
   		}
	}
	public void jobLaunch(String name){
		try{
			String line;
			Process auroraJob = Runtime.getRuntime().exec("aurora job create example/benchmarks/devel/"+name+" "+name+".aurora");
			auroraJob.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
			while ((line = stdout.readLine()) != null) {
				System.out.println(line); 
			}
		}catch (Exception ex) {
   			ex.printStackTrace();
   		}
	}
	public void configCreate(String name, String ram, String cpu, String disk, String image){
	try {
		String line1 = "import hashlib\n";
                String line2 = name+"= Process(name = '"+name+"', cmdline = 'java -jar /dacapo-9.12-bach.jar "+name+" -s small')\n";
                String line3 = name+"_task = SequentialTask(processes = [ "+name+"], resources = Resources(cpu = "+cpu+", ram = "+ram+"*MB, disk="+disk+"*MB))\n";
                String line4 = "jobs = [ Job(cluster = 'example', environment = 'devel', role = 'benchmarks', name = '"+name+"', task = "+name+"_task, instances =1 , container = Container(docker = Docker(image = '"+image+"')))]\n";

		File file = new File(name+".aurora");

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(line1);
                bw.write(line2);
                bw.write(line3);
                bw.write(line4);
		bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}			
