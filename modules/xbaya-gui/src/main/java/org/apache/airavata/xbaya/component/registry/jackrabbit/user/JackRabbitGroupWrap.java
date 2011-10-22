package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.airavata.registry.api.user.Authorizable;
import org.apache.airavata.registry.api.user.Group;

public class JackRabbitGroupWrap extends AbstractJackRabbitUMComponent implements Group {
    private org.apache.jackrabbit.api.security.user.Group group;

    public JackRabbitGroupWrap(org.apache.jackrabbit.api.security.user.Group group) {
        this.group = group;
    }

    @Override
    public String getID() throws RepositoryException {
        return group.getID();
    }

    @Override
    public boolean isGroup() {
        return group.isGroup();
    }

    @Override
    public Principal getPrincipal() throws RepositoryException {
        return group.getPrincipal();
    }

    @Override
    public void remove() throws RepositoryException {
        group.remove();
    }

    @Override
    public Iterator<String> getPropertyNames() throws RepositoryException {
        return group.getPropertyNames();
    }

    @Override
    public boolean hasProperty(String name) throws RepositoryException {
        return group.hasProperty(name);
    }

    @Override
    public void setProperty(String name, Value value) throws RepositoryException {
        group.setProperty(name, value);
    }

    @Override
    public void setProperty(String name, Value[] value) throws RepositoryException {
        group.setProperty(name, value);
    }

    @Override
    public Value[] getProperty(String name) throws RepositoryException {
        return group.getProperty(name);
    }

    @Override
    public boolean removeProperty(String name) throws RepositoryException {
        return group.removeProperty(name);
    }

    @Override
    public Iterator<Authorizable> getDeclaredMembers() throws RepositoryException {
        Iterator<org.apache.jackrabbit.api.security.user.Authorizable> declaredMembers = group.getDeclaredMembers();
        return getAuthorizableList(declaredMembers).iterator();
    }

    @Override
    public Iterator<Authorizable> getMembers() throws RepositoryException {
        return getAuthorizableList(group.getMembers()).iterator();
    }

    @Override
    public boolean isMember(Authorizable authorizable) throws RepositoryException {
        return group.isMember(new JackRabbitAuthorizable(authorizable));
    }

    @Override
    public boolean addMember(Authorizable authorizable) throws RepositoryException {
        return group.addMember(new JackRabbitAuthorizable(authorizable));
    }

    @Override
    public boolean removeMember(Authorizable authorizable) throws RepositoryException {
        return group.removeMember(new JackRabbitAuthorizable(authorizable));
    }

    @Override
    public Iterator<Group> declaredMemberOf() throws RepositoryException {
        return getGroupList(group.declaredMemberOf()).iterator();
    }

    @Override
    public Iterator<Group> memberOf() throws RepositoryException {
        return getGroupList(group.memberOf()).iterator();
    }

    @Override
    public Iterator<String> getPropertyNames(String name) throws RepositoryException {
        return group.getPropertyNames(name);
    }

    @Override
    public boolean isDeclaredMember(Authorizable authorizable) throws RepositoryException {
        return group.isDeclaredMember(new JackRabbitAuthorizable(authorizable));
    }

}
