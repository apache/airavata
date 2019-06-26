package org.apache.airavata.pga.tests;

import java.util.concurrent.TimeUnit;

import org.apache.airavata.pga.tests.utils.ExpFileReadUtils;
import org.junit.*;
import static org.junit.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class CreateUserLogin {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = ExpFileReadUtils.readProperty("base.url");
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testCreateUserLogin() throws Exception {

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

      driver.get(baseUrl);
      driver.findElement(By.linkText("Create account")).click();
      driver.findElement(By.id("username")).sendKeys(username);
      waitTime(500);
      driver.findElement(By.id("password")).sendKeys(password);
      waitTime(500);
      driver.findElement(By.id("confirm_password")).sendKeys(password);
      waitTime(500);
      driver.findElement(By.id("email")).sendKeys("pgauser@gmail.com");
      waitTime(500);
      driver.findElement(By.id("first_name")).sendKeys("PGA");
      waitTime(500);
      driver.findElement(By.id("last_name")).sendKeys("User");
      waitTime(500);
      driver.findElement(By.name("Submit")).click();
      waitTime(5000);
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
      fail(verificationErrorString);
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
