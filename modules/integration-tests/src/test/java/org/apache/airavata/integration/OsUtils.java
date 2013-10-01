package org.apache.airavata.integration;

import java.io.File;
import java.io.IOException;

public final class OsUtils
{
   private static String OS = null;
   public static String getOsName()
   {
      if(OS == null) { OS = System.getProperty("os.name"); }
      return OS;
   }
   public static boolean isWindows()
   {
      return getOsName().startsWith("Windows");
   }
   
   public static String getEchoExecutable() {
	   if(isWindows())
		   return "echo";
	    else 
	    	return "/bin/echo";
   }
   
   public static String getTempFolderPath() throws IOException {
	   return File.createTempFile("tmp",null).getParent();
   }

}