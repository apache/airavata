package org.apache.airavata.service.security.utils;

import org.apache.airavata.model.security.AuthzToken;


public class ThriftCustosDataModelConversion {

    public static org.apache.custos.commons.model.security.AuthzToken getCustosAuthzToken(AuthzToken authzToken) {
        org.apache.custos.commons.model.security.AuthzToken custosAuthz = new org.apache.custos.commons.model.security.AuthzToken();
        custosAuthz.setAccessToken(authzToken.getAccessToken());
        custosAuthz.setClaimsMap(authzToken.getClaimsMap());
        return custosAuthz;
    }

    public static AuthzToken getAuthzToken(org.apache.custos.commons.model.security.AuthzToken userManagementServiceAccountAuthzToken) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(userManagementServiceAccountAuthzToken.getAccessToken());
        authzToken.setClaimsMap(userManagementServiceAccountAuthzToken.getClaimsMap());
        return authzToken;
    }
}
