package org.apache.airavata.pga.tests.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/*
 **********User Login to PHP-Reference-Gateway**********
 * Created by Eroma on 9/12/14.
 * User Login in to PHP-Reference-Gateway. This class is called by all other test classes to login into the gateway.
 * Enter your Username & Pwd in this script
*/

public abstract class UserLogin {

    public void     authenticate(WebDriver driver){

        String username = null;
        String password = null;
        try {
            username = ExpFileReadUtils.readProperty("pga.username");
            password = ExpFileReadUtils.readProperty("pga.password");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (username == null || username.trim().equals("")
                || password == null || password.trim().equals(""))
            throw new RuntimeException("PGS user name or password in exp.properties file is invalid !");

        username = username.trim();
        password = password.trim();

        driver.findElement(By.linkText("Log in")).click();
          waitTime (500);
        driver.findElement(By.name("username")).clear();
          waitTime (500);
        driver.findElement(By.name("username")).sendKeys(username);
          waitTime (500);
        driver.findElement(By.name("password")).sendKeys(password);
          waitTime (500);
        driver.findElement(By.name("Submit")).click();

    }

    private void waitTime(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
