package org.apache.airavata.allocation.manager.notification.sender;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.airavata.allocation.manager.notification.models.EmailCredentials;

public class EmailNotificationConfiguration {
public static void main(String args[]) {
	EmailNotificationConfiguration obj = new EmailNotificationConfiguration();

}

	public EmailCredentials getCredentials() 
	{
		EmailCredentials result = new EmailCredentials();
		Properties prop = new Properties();
		InputStream input = null;

		try {
			InputStream input2 = new FileInputStream("./config.properties");
			prop.load(input2);

			result.setUserName(prop.getProperty("username"));
			result.setPassword(prop.getProperty("password"));

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
