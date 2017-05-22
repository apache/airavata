package org.apache.airavata.cloud.aurora.utilities;

import java.io.BufferedReader;

import org.apache.airavata.cloud.aurora.exception.AuroraException;

public interface AuroraUtilI{
  public void printLog(BufferedReader stdout) throws AuroraException;
}
