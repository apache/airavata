package org.apache.airavata.security.configurations;

import org.apache.airavata.security.AbstractDatabaseAuthenticator;
import org.apache.airavata.security.AuthenticationException;

public class TestDBAuthenticator1 extends AbstractDatabaseAuthenticator {

    public TestDBAuthenticator1() {
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

