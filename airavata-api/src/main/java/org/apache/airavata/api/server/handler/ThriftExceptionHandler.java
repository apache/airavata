package org.apache.airavata.api.server.handler;

import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for Thrift server handlers.
 * Converts service layer exceptions to appropriate Thrift exceptions.
 */
public class ThriftExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ThriftExceptionHandler.class);

    public static RuntimeException convertException(Throwable e, String context) {
        // Re-throw Thrift exceptions as-is
        if (e instanceof org.apache.airavata.model.error.InvalidRequestException ||
            e instanceof org.apache.airavata.model.error.AiravataClientException ||
            e instanceof org.apache.airavata.model.error.AiravataSystemException ||
            e instanceof org.apache.airavata.model.error.AuthorizationException ||
            e instanceof org.apache.airavata.model.error.ExperimentNotFoundException ||
            e instanceof org.apache.airavata.model.error.ProjectNotFoundException) {
            throw sneakyThrow(e);
        }

        // Convert service exceptions to AiravataSystemException
        if (e instanceof RegistryException || e instanceof AppCatalogException ||
            e instanceof org.apache.airavata.credential.store.store.CredentialStoreException ||
            e instanceof org.apache.airavata.sharing.registry.models.SharingRegistryException) {
            logger.error(context, e);
            org.apache.airavata.model.error.AiravataSystemException exception = new org.apache.airavata.model.error.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.model.error.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(context + ". More info : " + e.getMessage());
            throw sneakyThrow(exception);
        }

        // Handle any other exception
        logger.error(context, e);
        org.apache.airavata.model.error.AiravataSystemException exception = new org.apache.airavata.model.error.AiravataSystemException();
        exception.setAiravataErrorType(org.apache.airavata.model.error.AiravataErrorType.INTERNAL_ERROR);
        exception.setMessage(context + ". More info : " + e.getMessage());
        throw sneakyThrow(exception);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
