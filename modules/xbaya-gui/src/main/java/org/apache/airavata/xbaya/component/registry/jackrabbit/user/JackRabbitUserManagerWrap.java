package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.airavata.registry.api.user.Authorizable;
import org.apache.airavata.registry.api.user.AuthorizableExistsException;
import org.apache.airavata.registry.api.user.Group;
import org.apache.airavata.registry.api.user.User;
import org.apache.airavata.registry.api.user.UserManager;
import org.apache.airavata.registry.api.user.UserManagerFactory;
import org.apache.jackrabbit.api.JackrabbitSession;

public class JackRabbitUserManagerWrap extends AbstractJackRabbitUMComponent implements UserManager {

	static{
		UserManagerFactory.registerUserManager("org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory", JackRabbitUserManagerWrap.class);
	}
	
	@Override
	public User createUser(Session session, String userID, String password)
			throws AuthorizableExistsException, RepositoryException {
		org.apache.jackrabbit.api.security.user.User user = getJackRabbitUserManager(session).createUser(userID, password);
		return new JackRabbitUserWrap(user);
	}

	@Override
	public User createUser(Session session, String userID, String password,
			Principal principal, String intermediatePath)
			throws AuthorizableExistsException, RepositoryException {
		org.apache.jackrabbit.api.security.user.User user = getJackRabbitUserManager(session).createUser(userID, password,principal,intermediatePath);
		return new JackRabbitUserWrap(user);
	}

	@Override
	public Group createGroup(Session session, Principal principal)
			throws AuthorizableExistsException, RepositoryException {
		org.apache.jackrabbit.api.security.user.Group group = getJackRabbitUserManager(session).createGroup(principal);
		return new JackRabbitGroupWrap(group);
	}

	@Override
	public Group createGroup(Session session, Principal principal,
			String intermediatePath) throws AuthorizableExistsException,
			RepositoryException {
		return new JackRabbitGroupWrap(getJackRabbitUserManager(session).createGroup(principal, intermediatePath));
	}

	private org.apache.jackrabbit.api.security.user.UserManager getJackRabbitUserManager(Session session) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException{
		return ((JackrabbitSession) session).getUserManager(); 
	}

	@Override
	public Authorizable getAuthorizable(Session session, String id)
			throws RepositoryException {
		return new JackRabbitAuthorizableWrap(getJackRabbitUserManager(session).getAuthorizable(id));
	}

	@Override
	public Authorizable getAuthorizable(Session session, Principal principal)
			throws RepositoryException {
		return new JackRabbitAuthorizableWrap(getJackRabbitUserManager(session).getAuthorizable(principal));
	}

	@Override
	public Iterator<Authorizable> findAuthorizables(Session session,
			String propertyName, String value) throws RepositoryException {
		Iterator<org.apache.jackrabbit.api.security.user.Authorizable> authorizables = getJackRabbitUserManager(session).findAuthorizables(propertyName, value);
		return getAuthorizableList(authorizables).iterator();
	}

	@Override
	public Iterator<Authorizable> findAuthorizables(Session session,
			String propertyName, String value, int searchType)
			throws RepositoryException {
		Iterator<org.apache.jackrabbit.api.security.user.Authorizable> authorizables = getJackRabbitUserManager(session).findAuthorizables(propertyName, value,searchType);
		return getAuthorizableList(authorizables).iterator();
	}

	@Override
	public boolean isAutoSave(Session session) throws RepositoryException{
		return getJackRabbitUserManager(session).isAutoSave();
	}

	@Override
	public void autoSave(Session session, boolean enable)
			throws UnsupportedRepositoryOperationException, RepositoryException {
		getJackRabbitUserManager(session).autoSave(enable);
	}
}
