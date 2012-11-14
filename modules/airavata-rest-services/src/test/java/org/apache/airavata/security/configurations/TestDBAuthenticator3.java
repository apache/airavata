package org.apache.airavata.security.configurations;

import org.apache.airavata.security.AbstractDatabaseAuthenticator;
import org.apache.airavata.security.AuthenticationException;

/**
 * Created with IntelliJ IDEA.
 * User: thejaka
 * Date: 9/6/12
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestDBAuthenticator3 extends AbstractDatabaseAuthenticator {

    public TestDBAuthenticator3() {
        super();
    }

    @Override
    public void onSuccessfulAuthentication(Object authenticationInfo) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onFailedAuthentication(Object authenticationInfo) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean authenticate(Object credentials) throws AuthenticationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean doAuthentication(Object credentials) throws AuthenticationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthenticated(Object credentials) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
