package org.apache.airavata.services.registry.rest.security;

import junit.framework.TestCase;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.security.Authenticator;
import org.apache.airavata.security.configurations.AuthenticatorConfigurationReader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * An abstract class to implement test cases for authenticators.
 */
public abstract class AbstractAuthenticatorTest extends DatabaseTestCases {

    private String authenticatorName;

    protected Authenticator authenticator = null;

    public AbstractAuthenticatorTest(String name) throws Exception {
        authenticatorName = name;
    }

    protected AuthenticatorConfigurationReader authenticatorConfigurationReader;


    @Before
    public void setUp() throws Exception {

        authenticatorConfigurationReader = new AuthenticatorConfigurationReader();
        authenticatorConfigurationReader.init(this.getClass().getClassLoader().getResourceAsStream("authenticators.xml"));

        List<Authenticator> listAuthenticators = authenticatorConfigurationReader.getAuthenticatorList();

        if (listAuthenticators == null) {
            throw new Exception("No authenticators found !");
        }

        for (Authenticator a : listAuthenticators) {
            if (a.getAuthenticatorName().equals(authenticatorName)) {
                authenticator = a;
            }
        }

        if (authenticator == null) {
            throw new Exception("Could not find an authenticator with name " + authenticatorName);
        }

    }

    @Test
    public abstract void testAuthenticateSuccess() throws Exception;

    @Test
    public abstract void testAuthenticateFail() throws Exception;

    @Test
    public abstract void testCanProcess() throws Exception;
}
