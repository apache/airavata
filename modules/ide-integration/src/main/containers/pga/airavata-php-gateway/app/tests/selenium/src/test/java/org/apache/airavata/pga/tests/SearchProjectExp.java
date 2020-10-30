package org.apache.airavata.pga.tests;

import java.util.concurrent.TimeUnit;
import org.apache.airavata.pga.tests.utils.UserLogin;
import org.apache.airavata.pga.tests.utils.ExpFileReadUtils;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

/*
 **********Search for Project which has Experiments executed**********
 * Created by Eroma on 9/16/14.
 * The script will search for a project which will list all experiments created; can view the experiment status
 * project-name is read from exp.properties file
 * Changed by Eroma to read the base URL and sub URL from exp.properties file
 * Updated to work with Latest PGA by Eroma 08/05/2015
*/

public class SearchProjectExp extends UserLogin {
  private WebDriver driver;
  private String baseUrl;
  private String subUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();
  private String projectName;

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = ExpFileReadUtils.readProperty("base.url");
    subUrl = ExpFileReadUtils.readProperty("sub.url");
    projectName = ExpFileReadUtils.readProperty("project.name");
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testSearchProjectExp() throws Exception {
    driver.get(baseUrl + subUrl);
      authenticate(driver);
    driver.findElement(By.linkText("Project")).click();
    driver.findElement(By.id("browse")).click();
    new Select(driver.findElement(By.id("search-key"))).selectByVisibleText("Project Name");
      driver.findElement(By.id("search-value")).clear();
      driver.findElement(By.id("search-value")).sendKeys(projectName);
      driver.findElement(By.name("search")).click();
      try {
          assertTrue(isElementPresent(By.cssSelector("td")));
      } catch (Error e) {
          verificationErrors.append(e.toString());
      }
      try {
          assertEquals(projectName, driver.findElement(By.cssSelector("td")).getText());
      } catch (Error e) {
          verificationErrors.append(e.toString());
      }
      driver.findElement(By.linkText("View")).click();
      waitTime (30000);
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
