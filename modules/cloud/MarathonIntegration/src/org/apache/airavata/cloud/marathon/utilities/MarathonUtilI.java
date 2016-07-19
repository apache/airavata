package org.apache.airavata.cloud.marathon.utilities;

import java.io.BufferedReader;

import org.apache.airavata.cloud.marathon.exception.MarathonException;

public interface MarathonUtilI{
  public void printLog(BufferedReader stdout) throws MarathonException;
}
