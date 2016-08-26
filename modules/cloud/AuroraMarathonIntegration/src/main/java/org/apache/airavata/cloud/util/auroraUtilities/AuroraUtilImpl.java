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

package org.apache.airavata.cloud.util.auroraUtilities;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.airavata.cloud.exceptions.auroraExceptions.AuroraException;

public class AuroraUtilImpl implements AuroraUtilI{
  public void printLog(BufferedReader stdout) throws AuroraException
  {
    try{
      String line;
      line = stdout.readLine();
      while (line != null) {
          System.out.println(line);
          line = stdout.readLine();
        }
      }
      catch (IOException ex) {
  			throw new AuroraException("IO Exception occured while passing the command.\n"+ex.toString());
  		}
  }

  public BufferedReader executeProcess(String commandToRunProcess) throws AuroraException
  {
	BufferedReader stdout = null;
	try{
		Process auroraJob = Runtime.getRuntime().exec(commandToRunProcess);
		auroraJob.waitFor();
		stdout = new BufferedReader(new InputStreamReader(auroraJob.getInputStream()));
	}
	catch(Exception ex)
	{
		throw new AuroraException("Exception occured while passing the command.\n"+ex.toString());

	}
	return stdout;
  }

}
