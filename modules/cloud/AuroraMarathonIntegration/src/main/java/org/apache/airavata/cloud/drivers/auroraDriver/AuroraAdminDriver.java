/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.cloud.drivers.auroraDriver;

// TODO: need javadoc documentation at the top of each method

// TODO: individually import the types
import java.util.*;

import org.apache.airavata.cloud.exceptions.auroraExceptions.AuroraException;
import org.apache.airavata.cloud.bigDataClientSideServices.aurora.auroraClient.AuroraJobSchedulerI;
import org.apache.airavata.cloud.bigDataClientSideServices.aurora.auroraClient.AuroraJobSchedulerImpl;
import org.apache.airavata.cloud.bigDataInjections.AuroraInjectorImpl;
import org.apache.airavata.cloud.bigDataInjections.BigDataInjectorI;


import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;
import org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient.MarathonJobSchedulerI;
import org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient.MarathonJobSchedulerImpl;
import org.apache.airavata.cloud.bigDataInjections.MarathonInjectorImpl;

public class AuroraAdminDriver{
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

		// use the code below to decide between injecting Aurora, Marathon, etc. as injections

		AuroraJobSchedulerI auroraJS = new AuroraJobSchedulerImpl();
		BigDataInjectorI auroraInjector = new AuroraInjectorImpl(auroraJS);
		auroraInjector.executeTheBigDataClientSideCommand(params);



		// UNCOMMENT NEXT 3 LINES TO USE MARATHON and comment the 3 lines above to shut AURORA off.
		/*MarathonJobSchedulerI marathonJS = new MarathonJobSchedulerImpl();
		BigDataInjectorI marathonInjector = new MarathonInjectorImpl(marathonJS);
		marathonInjector.executeTheBigDataClientSideCommand(params);*/



	} // end of public static void main
} // end of class
