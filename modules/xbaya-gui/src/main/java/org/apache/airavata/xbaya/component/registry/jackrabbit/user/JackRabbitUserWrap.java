package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.airavata.registry.api.user.Group;
import org.apache.airavata.registry.api.user.User;

public class JackRabbitUserWrap extends AbstractJackRabbitUMComponent implements User{
	private org.apache.jackrabbit.api.security.user.User user;
	public JackRabbitUserWrap(org.apache.jackrabbit.api.security.user.User user) {
		this.user=user;
	}
	
	@Override
	public String getID() throws RepositoryException {
		return user.getID();
	}

	@Override
	public boolean isGroup() {
		return user.isGroup();
	}

	@Override
	public Principal getPrincipal() throws RepositoryException {
		return user.getPrincipal();
	}

	@Override
	public void remove() throws RepositoryException {
		user.remove();
		
	}

	@Override
	public Iterator<String> getPropertyNames() throws RepositoryException {
		return user.getPropertyNames();
	}

	@Override
	public boolean hasProperty(String name) throws RepositoryException {
		return user.hasProperty(name);
	}

	@Override
	public void setProperty(String name, Value value)
			throws RepositoryException {
		user.setProperty(name, value);
	}

	@Override
	public void setProperty(String name, Value[] value)
			throws RepositoryException {
		user.setProperty(name, value);
	}

	@Override
	public Value[] getProperty(String name) throws RepositoryException {
		return user.getProperty(name);
	}

	@Override
	public boolean removeProperty(String name) throws RepositoryException {
		return user.removeProperty(name);
	}

	@Override
	public boolean isAdmin() {
		return user.isAdmin();
	}

	@Override
	public Credentials getCredentials() throws RepositoryException {
		return user.getCredentials();
	}

	@Override
	public void changePassword(String password) throws RepositoryException {
		user.changePassword(password);
	}

	@Override
	public Iterator<Group> declaredMemberOf() throws RepositoryException {
		Iterator<org.apache.jackrabbit.api.security.user.Group> declaredMemberOf = user.declaredMemberOf();
		return getGroupList(declaredMemberOf).iterator();
	}

	@Override
	public Iterator<Group> memberOf() throws RepositoryException {
		Iterator<org.apache.jackrabbit.api.security.user.Group> declaredMemberOf = user.memberOf();
		return getGroupList(declaredMemberOf).iterator();
	}

	@Override
	public Iterator<String> getPropertyNames(String name)
			throws RepositoryException {
		return user.getPropertyNames(name);
	}

}
