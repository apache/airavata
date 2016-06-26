
package org.apache.airavata.cloud.aurora.utilities;


import java.io.BufferedReader;
import java.io.IOException;

import org.apache.airavata.cloud.aurora.exception.AuroraException;

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
}
