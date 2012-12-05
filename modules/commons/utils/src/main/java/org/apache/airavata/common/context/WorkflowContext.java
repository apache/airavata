package org.apache.airavata.common.context;

/**
 * The workflow context class. This will be local to a thread.
 * Workflow data that needs to propagate relevant to a request will be stored here.
 * We use thread local globals to store request data.
 * Currently we only store user identity.
 */
public class WorkflowContext {

    private static final ThreadLocal userThreadLocal = new ThreadLocal();

    /**
     * Sets the context.
     * @param context The context to be set. - Careful when calling this.
     *                Make sure other data relevant to context is preserved.
     */
    public static void set(RequestContext context) {
        userThreadLocal.set(context);
    }

    /**
     * Clears the context
     */
    public static void unset() {
        userThreadLocal.remove();
    }

    /**
     * Gets the context associated with current context.
     * @return The context associated with current thread.
     */
    public static RequestContext get() {
        return (RequestContext) userThreadLocal.get();
    }

    /**
     * Gets the user associated with current user.
     * @return User id associated with current request.
     */
    public static synchronized String getRequestUser() {

        RequestContext requestContext = (RequestContext) userThreadLocal.get();

        if (requestContext != null) {
            return requestContext.getUserIdentity();
        }

        return null;
    }

    public static synchronized String getGatewayId() {

        RequestContext requestContext = (RequestContext) userThreadLocal.get();

        if (requestContext != null) {
            return requestContext.getGatewayId();
        }

        return null;
    }
}
