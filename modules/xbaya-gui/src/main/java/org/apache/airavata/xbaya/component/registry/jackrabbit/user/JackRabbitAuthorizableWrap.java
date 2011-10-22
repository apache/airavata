package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.airavata.registry.api.user.Authorizable;
import org.apache.airavata.registry.api.user.Group;

public class JackRabbitAuthorizableWrap extends AbstractJackRabbitUMComponent implements Authorizable {
    private org.apache.jackrabbit.api.security.user.Authorizable authorizable;

    public JackRabbitAuthorizableWrap(org.apache.jackrabbit.api.security.user.Authorizable authorizable) {
        this.authorizable = authorizable;
    }

    @Override
    public String getID() throws RepositoryException {
        return authorizable.getID();
    }

    @Override
    public boolean isGroup() {
        return authorizable.isGroup();
    }

    @Override
    public Principal getPrincipal() throws RepositoryException {
        return authorizable.getPrincipal();
    }

    @Override
    public void remove() throws RepositoryException {
        authorizable.remove();
    }

    @Override
    public Iterator<String> getPropertyNames() throws RepositoryException {
        return authorizable.getPropertyNames();
    }

    @Override
    public boolean hasProperty(String name) throws RepositoryException {
        return authorizable.hasProperty(name);
    }

    @Override
    public void setProperty(String name, Value value) throws RepositoryException {
        authorizable.setProperty(name, value);
    }

    @Override
    public void setProperty(String name, Value[] value) throws RepositoryException {
        authorizable.setProperty(name, value);
    }

    @Override
    public Value[] getProperty(String name) throws RepositoryException {
        return authorizable.getProperty(name);
    }

    @Override
    public boolean removeProperty(String name) throws RepositoryException {
        return authorizable.removeProperty(name);
    }

    @Override
    public Iterator<Group> declaredMemberOf() throws RepositoryException {
        Iterator<org.apache.jackrabbit.api.security.user.Group> declaredMemberOf = authorizable.declaredMemberOf();
        return getGroupList(declaredMemberOf).iterator();
    }

    @Override
    public Iterator<Group> memberOf() throws RepositoryException {
        Iterator<org.apache.jackrabbit.api.security.user.Group> declaredMemberOf = authorizable.memberOf();
        return getGroupList(declaredMemberOf).iterator();
    }

    @Override
    public Iterator<String> getPropertyNames(String name) throws RepositoryException {
        return authorizable.getPropertyNames(name);
    }

}
