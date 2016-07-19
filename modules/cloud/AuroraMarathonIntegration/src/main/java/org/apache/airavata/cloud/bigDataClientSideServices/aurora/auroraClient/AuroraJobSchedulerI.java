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

package org.apache.airavata.cloud.bigDataClientSideServices.aurora.auroraClient;

import org.apache.airavata.cloud.exceptions.auroraExceptions.AuroraException;

public interface AuroraJobSchedulerI {


  public void auroraJobCommand(String info, String command) throws AuroraException;
	public void jobUpdateList(String info) throws AuroraException;
	public void jobUpdateAbort(String info) throws AuroraException;
	public void jobUpdateResume(String info) throws AuroraException;
	public void jobUpdatePause(String info) throws AuroraException;
	public void jobUpdateInfo(String info) throws AuroraException;
	public void jobUpdate(String update) throws AuroraException;
	public void jobRestart(String restart) throws AuroraException;
	public void jobKill(String kill) throws AuroraException;
	public void jobLaunch(String name) throws AuroraException;
	public void configCreate(String name, String ram, String cpu, String disk, String image) throws AuroraException;
  public void jobDiff(String key, String config) throws AuroraException;
  public void configList(String config) throws AuroraException;
  public void jobInspect(String key, String config) throws AuroraException;
  public void clusterQuota(String key) throws AuroraException;
  public void openWebUI(String key) throws AuroraException;
}
