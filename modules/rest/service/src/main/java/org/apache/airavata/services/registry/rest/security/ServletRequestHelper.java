package org.apache.airavata.services.registry.rest.security;

import org.apache.airavata.common.context.RequestContext;
import org.apache.airavata.common.context.WorkflowContext;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.security.AuthenticationException;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to extract request information.
 */
public class ServletRequestHelper {

    /**
     * Header names
     */
    public static final String AUTHORISATION_HEADER_NAME = "Authorization";

    protected void addIdentityInformationToSession(HttpServletRequest servletRequest) throws AuthenticationException {

        addUserToSession(null, servletRequest);
    }

    public void addUserToSession(String userName, HttpServletRequest servletRequest) throws AuthenticationException {

        if (userName == null) {
            userName = getUserName(servletRequest);
        }

        String gatewayId = getGatewayId(servletRequest);

        if (servletRequest.getSession() != null) {
            servletRequest.getSession().setAttribute(Constants.USER_IN_SESSION, userName);
            servletRequest.getSession().setAttribute(Constants.GATEWAY_NAME, gatewayId);
        }

        addToContext(userName, gatewayId);
    }

    String getUserName(HttpServletRequest httpServletRequest) throws AuthenticationException {

        String basicHeader = httpServletRequest.getHeader(AUTHORISATION_HEADER_NAME);

        if (basicHeader == null) {
            throw new AuthenticationException("Authorization Required");
        }

        String[] userNamePasswordArray = basicHeader.split(" ");

        if (userNamePasswordArray == null || userNamePasswordArray.length != 2) {
            throw new AuthenticationException("Authorization Required");
        }

        String decodedString = decode(userNamePasswordArray[1]);

        String[] array = decodedString.split(":");

        if (array == null || array.length != 1) {
            throw new AuthenticationException("Authorization Required");
        }

        return array[0];

    }

    public String decode(String encoded) {
        return new String(Base64.decodeBase64(encoded.getBytes()));
    }

    String getGatewayId(HttpServletRequest request) throws AuthenticationException {
        String gatewayId = request.getHeader(Constants.GATEWAY_NAME);

        if (gatewayId == null) {
            try {
                gatewayId = ServerSettings.getDefaultGatewayId();
            } catch (ApplicationSettingsException e) {
                throw new AuthenticationException("Unable to retrieve default gateway", e);
            }
        }

        return gatewayId;
    }

    public void addToContext(String userName, String gatewayId) {

        RequestContext requestContext = new RequestContext();
        requestContext.setUserIdentity(userName);
        requestContext.setGatewayId(gatewayId);

        WorkflowContext.set(requestContext);
    }


}
