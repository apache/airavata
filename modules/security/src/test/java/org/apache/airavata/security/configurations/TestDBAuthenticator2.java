package org.apache.airavata.security.configurations;

import org.apache.airavata.security.AbstractAuthenticator;
import org.apache.airavata.security.AuthenticationException;
import org.w3c.dom.Node;

/**
 * Created with IntelliJ IDEA.
 * User: thejaka
 * Date: 9/6/12
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestDBAuthenticator2 extends AbstractAuthenticator {

    public TestDBAuthenticator2() {
        super();
    }

    @Override
    protected boolean doAuthentication(Object credentials) throws AuthenticationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
    public boolean isAuthenticated(Object credentials) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void configure(Node node) throws RuntimeException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
