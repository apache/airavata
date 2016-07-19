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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.airavata.cloud.exceptions.marathonExceptions.MarathonException;
import org.apache.airavata.cloud.util.marathonUtilities.MarathonUtilImpl;
import org.apache.airavata.cloud.util.marathonUtilities.MarathonUtilI;

public class MarathonJobSchedulerImpl implements MarathonJobSchedulerI {
	MarathonUtilI util = new MarathonUtilImpl();


	public void marathonJobCommand(String info, String command) throws MarathonException{
		try{

				//TODO:Command inmplementation
		}

		catch (Exception ex) {
			throw new MarathonException("Exception occured while passing the command.\n"+ex.toString());
		}
	}

	//TODO:Rest of the commands inmplementation goes here

}
