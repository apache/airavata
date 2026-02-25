package org.apache.airavata.restapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.restapi.util.AuthzTokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Populates the research-service {@link UserContext} ThreadLocal from the
 * already-validated JWT for research API paths.
 */
@Component
public class ResearchContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var authzToken = AuthzTokenUtil.extractAuthzToken(request);
        UserContext.setAuthzToken(authzToken);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
