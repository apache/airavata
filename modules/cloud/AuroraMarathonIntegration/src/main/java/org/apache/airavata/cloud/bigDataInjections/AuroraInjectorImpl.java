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


import org.apache.airavata.cloud.bigDataClientSideServices.aurora.auroraClient.AuroraJobSchedulerI;
import org.apache.airavata.cloud.bigDataClientSideServices.aurora.auroraClient.AuroraJobSchedulerImpl;
import org.apache.airavata.cloud.exceptions.auroraExceptions.AuroraException;


public class AuroraInjectorImpl implements BigDataInjectorI {
    private AuroraJobSchedulerI auroraJS = null;

    public AuroraInjectorImpl(AuroraJobSchedulerI auroraJSIn) {
	auroraJS = auroraJSIn;
    }


    public void executeTheBigDataClientSideCommand(Map<String, List<String>> commandLineOptions) {


	String commandName = commandLineOptions.get("o").get(0);
	String RamSize, JobName, CpuCount, DiskSize, Image;

		switch(commandName)
		{
			case "kill" :
				try {
					auroraJS.jobKill(commandLineOptions.get("n").get(0));
				} catch(AuroraException ex){
				} break;
			case "restart" :
				try {
					auroraJS.jobRestart(commandLineOptions.get("n").get(0));
				} catch (AuroraException ex) {
				} break;
			case "update" :
				try {
					auroraJS.jobUpdate(commandLineOptions.get("n").get(0));
				} catch(AuroraException ex){
				} break;
			case "update-info" :
				try {
					auroraJS.jobUpdateInfo(commandLineOptions.get("n").get(0));
				} catch(AuroraException ex){
				} break;
			case "update-pause" :
				try {
					auroraJS.jobUpdatePause(commandLineOptions.get("n").get(0));
				} catch(AuroraException ex){
				} break;
      case "inspect" :
				try {
					auroraJS.jobInspect(commandLineOptions.get("n").get(0), commandLineOptions.get("k").get(0));
				} catch(AuroraException ex){
				} break;
			case "quota" :
				try {
					auroraJS.clusterQuota(commandLineOptions.get("k").get(0));
				} catch(AuroraException ex){
				} break;
			case "create" :
				JobName = commandLineOptions.get("n").get(0);
				RamSize = commandLineOptions.get("r").get(0);
				CpuCount = commandLineOptions.get("c").get(0);
				DiskSize = commandLineOptions.get("d").get(0);
				Image = commandLineOptions.get("i").get(0);
				try {
					auroraJS.configCreate(JobName,RamSize,CpuCount,DiskSize,Image);
				} catch (AuroraException ex) {}
		 		try {
					auroraJS.jobLaunch(JobName);
				} catch (AuroraException ex) {
				} break;
			default :
				System.out.println("Improper option\nOptions available:\n1) create\n2) kill\n3) restart\n4) update\n 5) update-info\n6) update-pause\n");
		}
    } // end of public void executeTheBigDataCommand
} // end of public class AuroraInjectorImpl
