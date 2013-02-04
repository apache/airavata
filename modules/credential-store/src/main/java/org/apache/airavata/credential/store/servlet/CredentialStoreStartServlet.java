package org.apache.airavata.credential.store.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.security.servlet.JSPUtil;
import edu.uiuc.ncsa.security.util.pkcs.KeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment.CALLBACK_URI_KEY;

/**
 * When portal initiate a request to get credentials it will hit this servlet.
 */
public class CredentialStoreStartServlet extends ClientServlet {

    private String errorUrl;
    private String redirectUrl;

    private static Logger log = LoggerFactory.getLogger(CredentialStoreStartServlet.class);

    protected String decorateURI(URI inputURI, Map<String, String> parameters) {

        if (parameters.isEmpty()) {
            return inputURI.toString();
        }

        String stringUri = inputURI.toString();
        StringBuilder stringBuilder = new StringBuilder(stringUri);

        boolean isFirst = true;

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (isFirst) {
                stringBuilder.append("?");
                isFirst = false;
            } else {
                stringBuilder.append("&");
            }

            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return stringBuilder.toString();

    }

    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response)
            throws Throwable {

        String gatewayName = request.getParameter("gatewayName");
        String portalUserName = request.getParameter("portalUserName");
        String contactEmail = request.getParameter("email");

        if (gatewayName == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a gateway name."), request,
                    response, "/credential-store/error.jsp");
            return;
        }

        if (portalUserName == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a portal user name."), request,
                    response, "/credential-store/error.jsp");
            return;
        }

        if (contactEmail == null) {
            JSPUtil.handleException(new RuntimeException("Please specify a contact email address for community" +
                    " user account."), request,
                    response, "/credential-store/error.jsp");
            return;
        }

        log.info("1.a. Starting transaction");
        OA4MPResponse gtwResp = null;

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("gatewayName", gatewayName);
        queryParameters.put("portalUserName", portalUserName);
        queryParameters.put("email", contactEmail);

        Map<String, String> additionalParameters = new HashMap<String, String>();

        String modifiedCallbackUri = decorateURI(getOA4MPService().getEnvironment().getCallback(), queryParameters);

        info("The modified callback URI - " + modifiedCallbackUri);

        additionalParameters.put(getEnvironment().getConstants().get(CALLBACK_URI_KEY), modifiedCallbackUri);


        // Drumroll please: here is the work for this call.
        try {
            gtwResp = getOA4MPService().requestCert(additionalParameters);
        } catch (Throwable t) {
            JSPUtil.handleException(t, request, response, "/credential-store/error.jsp");
            return;
        }
        log.info("1.b. Got response. Creating page with redirect for " + gtwResp.getRedirect().getHost());
        // Normally, we'd just do a redirect, but we will put up a page and show the redirect to the user.
        // The client response contains the generated private key as well
        // In a real application, the private key would be stored. This, however, exceeds the scope of this
        // sample application -- all we need to do to complete the process is send along the redirect url.

        request.setAttribute(REDIR, REDIR);
        request.setAttribute("redirectUrl", gtwResp.getRedirect().toString());
        request.setAttribute(ACTION_KEY, ACTION_KEY);
        request.setAttribute("action", ACTION_REDIRECT_VALUE);
        log.info("1.b. Showing redirect page.");
        JSPUtil.fwd(request, response, "/credential-store/show-redirect.jsp");

    }
}
