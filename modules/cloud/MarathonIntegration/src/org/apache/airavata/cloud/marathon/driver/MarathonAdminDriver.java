package org.apache.airavata.cloud.marathon.driver;

// TODO: need javadoc documentation at the top of each method

// TODO: individually import the types
import java.util.*;

import org.apache.airavata.cloud.marathon.exception.MarathonException;
import org.apache.airavata.cloud.marathon.marathonClient.MarathonJobSchedulerImpl;
import org.apache.airavata.cloud.marathon.marathonClient.MarathonJobSchedulerI;
import org.apache.airavata.cloud.marathon.bigDataInjections.MarathonInjectorImpl;
import org.apache.airavata.cloud.marathon.bigDataInjections.BigDataInjectorI;

public class MarathonAdminDriver{
	public static void main(String[] args) {

	    // TODO: do command line validation

	    // Processing of the command line arguments should be moved to a different method



	    // This code to add command line arguments is based on Apache Commons

	    // TODO: explain why this Map data structure is needed
		Map<String, List<String>> params = new HashMap<>();
		
		// TODO: explain what is the purpose of this List
		List<String> options = null;
		for (int i = 0; i < args.length; i++) {
    			final String a = args[i];
		    	if (a.charAt(0) == '-') {
			        if (a.length() < 2) {
				    // TOOD: need more details in the error statement
			            System.err.println("Error at argument " + a);
				    return;
        			}
				// TODO: explain the purpose of this ArrayList
			        options = new ArrayList<>();
        			params.put(a.substring(1), options);
    			}
			// TODO: explain when this "else" branch is taken
    			else if (options != null) {
        			options.add(a);
    			}
    			else {

        			System.err.println("Illegal parameter \n[USAGE]\nOptions:\n1) -o\tcreate, kill, restart, update, update-info, update-pause\n2) -n\tname of the job\n 3) -r\tamount of RAM\n 4) -c\tCPU count\n 5) -d\tdisk space\n 6) -k\tname of the task to be killed\n 7) -i\texecutable/image\n ");
        			return;
    			}

			
		}// end of for (int i=0; ...
		
		MarathonJobSchedulerI marathonJS = new MarathonJobSchedulerImpl();
		BigDataInjectorI marathonInjector = new MarathonInjectorImpl(marathonJS);
		marathonInjector.executeTheBigDataClientSideCommand(params); 


	} // end of public static void main
} // end of class
