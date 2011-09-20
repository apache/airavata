package org.apache.airavata.registry.api.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserManagerFactory {
	private static Map<String,String> userManagers=new HashMap<String, String>();
	private static Log log = LogFactory.getLog(UserManagerFactory.class);

	public static UserManager getUserManager(String userManagerKey) throws Exception{
		if (userManagers.containsKey(userManagerKey)){
			String userManagerClass = userManagers.get(userManagerKey);
			try {
				return (UserManager)Class.forName(userManagerClass).newInstance();
			} catch (Exception e) {
				log.error("Error retrieving user manager for key "+userManagerKey,e);
				throw e;
			}
		}
		return null;
	}
	
	public static void registerUserManager(String userManagerKey, String userManagerClassName){
		userManagers.put(userManagerKey, userManagerClassName);
	}
	
	public static void registerUserManager(String userManagerKey, Class<?> userManagerClass){
		registerUserManager(userManagerKey,userManagerClass.getCanonicalName());
	}
	
	public static void registerUserManager(String userManagerKey, UserManager userManagerObj){
		registerUserManager(userManagerKey, userManagerObj.getClass());
	}
}
