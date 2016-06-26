package utilities;

import java.io.BufferedReader;
import exception.AuroraException;
import java.io.IOException;

public class Utility implements UtilityI{
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
