package org.apache.airavata.services.registry.rest.security.session;

import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.services.registry.rest.security.AbstractAuthenticatorTest;
import org.apache.airavata.services.registry.rest.security.MyHttpServletRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Session authenticator test.
 */
public class SessionAuthenticatorTest extends AbstractAuthenticatorTest {

    public SessionAuthenticatorTest() throws Exception {
        super("sessionAuthenticator");
    }

    @BeforeClass
    public static void setUpDatabase() throws Exception{
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();


        String createSessionTable = "create table Persons ( sessionId varchar(255) )";
        executeSQL(createSessionTable);

        String insertSessionSQL = "INSERT INTO Persons VALUES('1234')";
        executeSQL(insertSessionSQL);
    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }


    public void testAuthenticateSuccess() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "1234");

        Assert.assertTrue(authenticator.authenticate(servletRequestRequest));

    }

    public void testAuthenticateFail() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "12345");

        Assert.assertFalse(authenticator.authenticate(servletRequestRequest));

    }

    public void testCanProcess() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "12345");

        Assert.assertTrue(authenticator.canProcess(servletRequestRequest));

    }
}
