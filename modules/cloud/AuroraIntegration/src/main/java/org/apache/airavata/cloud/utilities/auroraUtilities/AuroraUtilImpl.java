
package org.apache.airavata.cloud.utilities.auroraUtilities;


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
