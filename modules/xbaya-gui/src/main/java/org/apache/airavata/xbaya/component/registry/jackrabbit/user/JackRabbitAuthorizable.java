package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;

public class JackRabbitAuthorizable extends AbstractJackRabbitUMComponent implements Authorizable {
	private org.apache.airavata.registry.api.user.Authorizable authorizable;
	
	public JackRabbitAuthorizable(org.apache.airavata.registry.api.user.Authorizable authorizable) {
		this.authorizable=authorizable;
	}
	
	@Override
	public Iterator<Group> declaredMemberOf() throws RepositoryException {
		Iterator<org.apache.airavata.registry.api.user.Group> declaredMemberOfGroupList = authorizable.declaredMemberOf();
		List<Group> groupList = getJRGroupList(declaredMemberOfGroupList);
		return groupList.iterator();
	}

	@Override
	public String getID() throws RepositoryException {
		return authorizable.getID();
	}

	@Override
	public Principal getPrincipal() throws RepositoryException {
		return authorizable.getPrincipal();
	}

	@Override
	public Value[] getProperty(String name) throws RepositoryException {
		return authorizable.getProperty(name);
	}

	@Override
	public Iterator<String> getPropertyNames() throws RepositoryException {
		return authorizable.getPropertyNames();
	}

	@Override
	public Iterator<String> getPropertyNames(String name)
			throws RepositoryException {
		return authorizable.getPropertyNames(name);
	}

	@Override
	public boolean hasProperty(String name) throws RepositoryException {
		return authorizable.hasProperty(name);
	}

	@Override
	public boolean isGroup() {
		return authorizable.isGroup();
	}

	@Override
	public Iterator<Group> memberOf() throws RepositoryException {
		Iterator<org.apache.airavata.registry.api.user.Group> declaredMemberOfGroupList = authorizable.memberOf();
		List<Group> groupList = getJRGroupList(declaredMemberOfGroupList);
		return groupList.iterator();
	}

	@Override
	public void remove() throws RepositoryException {
		authorizable.remove();
	}

	@Override
	public boolean removeProperty(String name) throws RepositoryException {
		return authorizable.removeProperty(name);
	}

	@Override
	public void setProperty(String name, Value value) throws RepositoryException {
		authorizable.setProperty(name, value);
	}

	@Override
	public void setProperty(String name, Value[] value)
			throws RepositoryException {
		authorizable.setProperty(name, value);
	}

}
