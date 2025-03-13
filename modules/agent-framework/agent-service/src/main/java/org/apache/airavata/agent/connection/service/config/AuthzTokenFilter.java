package org.apache.airavata.agent.connection.service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.airavata.agent.connection.service.UserContext;
import org.apache.airavata.model.security.AuthzToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class AuthzTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String xClaimsHeader = request.getHeader("X-Claims");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ") && xClaimsHeader != null) {
            try {
                String accessToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> claimsMap = objectMapper.readValue(xClaimsHeader, new TypeReference<>() {
                });

                AuthzToken authzToken = new AuthzToken();
                authzToken.setAccessToken(accessToken);
                authzToken.setClaimsMap(claimsMap);

                UserContext.setAuthzToken(authzToken);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization data");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

