package org.apache.airavata.security;

/**
 * An interface which can be used to authorise accessing resources.
 */
@SuppressWarnings("UnusedDeclaration")
public interface Authoriser {

    /**
     * Checks whether user has sufficient privileges to perform action on the given resource.
     * @param userName  The user who is performing the action.
     * @param resource The resource which user is trying to access.
     * @param action  The action (GET, PUT etc ...)
     * @return Returns <code>true</code> if user is authorised to perform the action, else false.
     */
    boolean isAuthorised (String userName, String resource, String action);

}
