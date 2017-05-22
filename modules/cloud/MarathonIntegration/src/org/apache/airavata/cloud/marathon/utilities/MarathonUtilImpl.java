
package org.apache.airavata.cloud.marathon.utilities;


import java.io.BufferedReader;
import java.io.IOException;

import org.apache.airavata.cloud.marathon.exception.MarathonException;

public class MarathonUtilImpl implements MarathonUtilI{
  public void printLog(BufferedReader stdout) throws MarathonException
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
  			throw new MarathonException("IO Exception occured while passing the command.\n"+ex.toString());
  		}
  }
}
