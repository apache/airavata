package org.apache.airavata.pga.tests;

import java.util.concurrent.TimeUnit;

import org.apache.airavata.pga.tests.utils.UserLogin;
import org.apache.airavata.pga.tests.utils.ExpFileReadUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

//import static org.testng.Assert.fail;

/*
 **********Create, Modify & Search Project**********
 * Created by Airavata on 9/12/14.
 * The script creates, modifies and searches for the created Project
 * project-name and project-description are read from the exp.properties file
 * Modified by Eroma on 10/23/14. Base URL & Sub URL to be read from the exp.properties file
*/


public class CreateModifySearchProject extends UserLogin {
  private WebDriver driver;
  private String subUrl;
  private String baseUrl;
  private String projectDescription;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = ExpFileReadUtils.readProperty("base.url");
    subUrl = ExpFileReadUtils.readProperty("sub.url");
    projectDescription = ExpFileReadUtils.readProperty("project.description");
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testCreateModifySearchProject() throws Exception {
    driver.get(baseUrl + subUrl);
      authenticate(driver);
    driver.findElement(By.linkText("Project")).click();
    driver.findElement(By.id("create")).click();
    driver.findElement(By.id("project-name")).clear();
    driver.findElement(By.id("project-name")).sendKeys(ExpFileReadUtils.readProperty("project.name"));
      waitTime (500);
    driver.findElement(By.id("project-description")).clear();
    driver.findElement(By.id("project-description")).sendKeys(projectDescription);
      waitTime (500);
    driver.findElement(By.name("save")).click();
      waitTime(750);
    driver.findElement(By.cssSelector("span.glyphicon.glyphicon-pencil")).click();
    driver.findElement(By.id("project-description")).clear();
    driver.findElement(By.id("project-description")).sendKeys(projectDescription + "_MODIFIED_2015");
      waitTime(500);
    driver.findElement(By.name("save")).click();
      waitTime(500);
    driver.findElement(By.linkText("Project")).click();
    driver.findElement(By.id("browse")).click();
      waitTime(500);
    driver.findElement(By.id("search-value")).clear();
    driver.findElement(By.id("search-value")).sendKeys(ExpFileReadUtils.readProperty("project.name"));
      waitTime(500);
    driver.findElement(By.name("search")).click();
    driver.findElement(By.linkText("View")).click();
      waitTime(500);
  }

    private void waitTime(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      throw new Exception(verificationErrorString);

    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
