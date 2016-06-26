package utilities;
import java.io.BufferedReader;
import exception.AuroraException;

public interface AuroraUtilI{
  public void printLog(BufferedReader stdout) throws AuroraException;
}
