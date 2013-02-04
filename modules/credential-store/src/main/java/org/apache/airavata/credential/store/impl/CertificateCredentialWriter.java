package org.apache.airavata.credential.store.impl;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.*;
import org.apache.airavata.credential.store.impl.db.CommunityUserDAO;
import org.apache.airavata.credential.store.impl.db.CredentialsDAO;

/**
 * Writes certificate credentials to database.
 */
public class CertificateCredentialWriter implements CredentialWriter {

    private CredentialsDAO credentialsDAO;
    private CommunityUserDAO communityUserDAO;

    public CertificateCredentialWriter(DBUtil dbUtil) {
        credentialsDAO = new CredentialsDAO(dbUtil);
        communityUserDAO = new CommunityUserDAO(dbUtil);
    }

    @Override
    public void writeCredentials(Credential credential) throws CredentialStoreException {

        CertificateCredential certificateCredential = (CertificateCredential)credential;

        // Write community user
        writeCommunityUser(certificateCredential.getCommunityUser());

        // First delete existing credentials
        credentialsDAO.deleteCredentials(certificateCredential.getCommunityUser().getGatewayName(),
                certificateCredential.getCommunityUser().getUserName());

        // Add the new certificate
        CertificateCredential certificateCredentials = (CertificateCredential)credential;
        credentialsDAO.addCredentials(certificateCredentials);
    }

    @Override
    public void writeCommunityUser(CommunityUser communityUser) throws CredentialStoreException {

        // First delete existing community user
        communityUserDAO.deleteCommunityUser(communityUser);

        // Persist new community user
        communityUserDAO.addCommunityUser(communityUser);

    }
}
