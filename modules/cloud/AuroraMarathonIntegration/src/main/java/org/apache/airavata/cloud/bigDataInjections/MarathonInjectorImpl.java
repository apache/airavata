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

package org.apache.airavata.cloud.bigDataInjections;

import java.util.Map;
import java.util.List;


import org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient.MarathonJobSchedulerI;
import org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient.MarathonJobSchedulerImpl;
import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;


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
