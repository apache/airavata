package org.apache.airavata.cloud.utilities.auroraUtilities;

import java.io.BufferedReader;

import org.apache.airavata.cloud.exceptions.auroraExceptions.AuroraException;

public interface AuroraUtilI{
  public void printLog(BufferedReader stdout) throws AuroraException;
  public BufferedReader executeProcess(String commandToRunProcess) throws AuroraException;
}
