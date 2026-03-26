package org.apache.airavata.api.server.handler;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.exception.ServiceNotFoundException;
import org.apache.thrift.TException;

import java.util.Map;

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
            throws AiravataSystemException, AuthorizationException, ExperimentNotFoundException {
        try {
            RequestContext ctx = toRequestContext(authzToken, gatewayId);
            return call.apply(ctx);
        } catch (ServiceAuthorizationException e) {
            throw new AuthorizationException(e.getMessage());
        } catch (ServiceNotFoundException e) {
            throw new ExperimentNotFoundException(e.getMessage());
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
            throws AiravataSystemException, AuthorizationException, ExperimentNotFoundException {
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
