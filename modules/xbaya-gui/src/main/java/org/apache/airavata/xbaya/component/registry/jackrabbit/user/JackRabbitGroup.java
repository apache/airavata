package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;


public class JackRabbitGroup extends AbstractJackRabbitUMComponent implements Group {
	private org.apache.airavata.registry.api.user.Group group;

	public JackRabbitGroup(org.apache.airavata.registry.api.user.Group group) {
		this.group=group;
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
	public void setProperty(String name, Value value)
			throws RepositoryException {
		group.setProperty(name, value);
	}

	@Override
	public void setProperty(String name, Value[] value)
			throws RepositoryException {
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
	public Iterator<Authorizable> getDeclaredMembers()
			throws RepositoryException {
		Iterator<org.apache.airavata.registry.api.user.Authorizable> declaredMembers = group.getDeclaredMembers();
		return getJRAuthorizableList(declaredMembers).iterator();
	}

	@Override
	public Iterator<Authorizable> getMembers() throws RepositoryException {
		return getJRAuthorizableList(group.getMembers()).iterator();
	}

	@Override
	public boolean isMember(Authorizable authorizable)
			throws RepositoryException {
		return group.isMember(new JackRabbitAuthorizableWrap(authorizable));
	}

	@Override
	public boolean addMember(Authorizable authorizable)
			throws RepositoryException {
		return group.addMember(new JackRabbitAuthorizableWrap(authorizable));
	}

	@Override
	public boolean removeMember(Authorizable authorizable)
			throws RepositoryException {
		return group.removeMember(new JackRabbitAuthorizableWrap(authorizable));
	}

	@Override
	public Iterator<Group> declaredMemberOf() throws RepositoryException {
		return getJRGroupList(group.declaredMemberOf()).iterator();
	}

	@Override
	public Iterator<Group> memberOf() throws RepositoryException {
		return getJRGroupList(group.memberOf()).iterator();
	}

	@Override
	public Iterator<String> getPropertyNames(String name)
			throws RepositoryException {
		return group.getPropertyNames(name);
	}

	@Override
	public boolean isDeclaredMember(Authorizable authorizable)
			throws RepositoryException {
		return group.isDeclaredMember(new JackRabbitAuthorizableWrap(authorizable));
	}

}
