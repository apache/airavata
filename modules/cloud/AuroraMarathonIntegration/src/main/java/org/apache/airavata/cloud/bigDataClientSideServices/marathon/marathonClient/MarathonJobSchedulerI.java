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

package org.apache.airavata.cloud.bigDataClientSideServices.marathon.marathonClient;

import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;

public interface MarathonJobSchedulerI {

	public void jobKill(String kill, String address) throws MarathonException;
	public void jobLaunch(String name, String address) throws MarathonException;
	public void configCreate(String name, String ram, String cpu, String disk, String image, String command) throws MarathonException;
	public void jobList(String address) throws MarathonException;
	public void jobListById(String address, String id) throws MarathonException;
	public void jobListByName(String address, String name) throws MarathonException;
	public void jobDelete(String address, String appid) throws MarathonException;
	public void runningJobs(String address, String appid) throws MarathonException;
	public void createGroups(String address, String json) throws MarathonException;
	public void groups(String address) throws MarathonException;
	public void groupsId(String address, String groupid) throws MarathonException;
	public void jobDeleteId(String address, String appid, String taskid) throws MarathonException;
	public void deleteDeployment(String address, String id) throws MarathonException;
	public void deploymentList(String address) throws MarathonException;
	public void deleteGroups(String address, String id) throws MarathonException;
	public void eventsList(String address) throws MarathonException;
	public void eventSubscriptionList(String address) throws MarathonException;
	public void launchQueue(String address) throws MarathonException;
	public void deleteMarathonLeader(String address) throws MarathonException;
	public void marathonLeader(String address) throws MarathonException;
	public void marathonInfo(String address) throws MarathonException;


}
