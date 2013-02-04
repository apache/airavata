package org.apache.airavata.credential.store.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPService;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.delegation.client.request.DelegationRequest;
import edu.uiuc.ncsa.security.delegation.client.request.DelegationResponse;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.PKCS10CertificationRequest;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import static edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment.CALLBACK_URI_KEY;
import static edu.uiuc.ncsa.security.util.pkcs.CertUtil.createCertRequest;
import static edu.uiuc.ncsa.security.util.pkcs.KeyUtil.generateKeyPair;

/**
 * Credential store specific OA4MPService.
 * Only change is add support to include get parameters.
 */
public class CredentialStoreOA4MPServer extends OA4MPService {
    public CredentialStoreOA4MPServer(ClientEnvironment environment) {
        super(environment);
    }

    public OA4MPResponse requestCert(Map additionalParameters) {
        if (additionalParameters == null) {
            additionalParameters = new HashMap();
        }
        try {
            KeyPair keyPair = generateKeyPair();
            PKCS10CertificationRequest certReq = createCertRequest(keyPair);
            OA4MPResponse mpdsResponse = new OA4MPResponse();
            mpdsResponse.setPrivateKey(keyPair.getPrivate());
            additionalParameters.put(ClientEnvironment.CERT_REQUEST_KEY, Base64.encodeBase64String(certReq.getDEREncoded()));

            if (additionalParameters.get(getEnvironment().getConstants().get(CALLBACK_URI_KEY)) == null) {
                additionalParameters.put(getEnvironment().getConstants().get(CALLBACK_URI_KEY), getEnvironment().
                        getCallback().toString());
            }

            DelegationRequest daReq = new DelegationRequest();
            daReq.setParameters(additionalParameters);
            daReq.setClient(getEnvironment().getClient());
            daReq.setBaseUri(getEnvironment().getAuthorizationUri());
            DelegationResponse daResp = (DelegationResponse) getEnvironment().getDelegationService().process(daReq);
            mpdsResponse.setRedirect(daResp.getRedirectUri());
            return mpdsResponse;
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new GeneralException("Error generating request", e);
        }

    }
}
