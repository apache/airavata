/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.api.server.handler;

import java.util.Map;
import org.apache.airavata.common.config.Constants;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceAuthorizationException;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.execution.service.ServiceNotFoundException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;

public class ThriftAdapter {

    @FunctionalInterface
    public interface ServiceCall<T> {
        T apply(RequestContext ctx) throws Exception;
    }

    @FunctionalInterface
    public interface ServiceVoidCall {
        void apply(RequestContext ctx) throws Exception;
    }

    public static <T> T execute(AuthzToken authzToken, String gatewayId, ServiceCall<T> call)
            throws AiravataSystemException, AuthorizationException {
        try {
            RequestContext ctx = toRequestContext(authzToken, gatewayId);
            return call.apply(ctx);
        } catch (ServiceAuthorizationException e) {
            throw new AuthorizationException(e.getMessage());
        } catch (ServiceNotFoundException e) {
            AiravataSystemException ase = new AiravataSystemException();
            ase.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ase.setMessage("Resource not found: " + e.getMessage());
            throw ase;
        } catch (ServiceException e) {
            AiravataSystemException ase = new AiravataSystemException();
            ase.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ase.setMessage(e.getMessage());
            throw ase;
        } catch (AuthorizationException | AiravataSystemException e) {
            throw e;
        } catch (Exception e) {
            AiravataSystemException ase = new AiravataSystemException();
            ase.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ase.setMessage(e.getMessage());
            throw ase;
        }
    }

    public static void executeVoid(AuthzToken authzToken, String gatewayId, ServiceVoidCall call)
            throws AiravataSystemException, AuthorizationException {
        execute(authzToken, gatewayId, ctx -> {
            call.apply(ctx);
            return null;
        });
    }

    private static RequestContext toRequestContext(AuthzToken authzToken, String gatewayId) {
        Map<String, String> claims = authzToken.getClaimsMap();
        String userId = claims.get(Constants.USER_NAME);
        String gw = claims.getOrDefault(Constants.GATEWAY_ID, gatewayId);
        return new RequestContext(userId, gw, authzToken.getAccessToken(), claims);
    }
}
