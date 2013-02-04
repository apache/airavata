package org.apache.airavata.credential.store.impl;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.*;
import org.apache.airavata.credential.store.impl.db.CommunityUserDAO;
import org.apache.airavata.credential.store.impl.db.CredentialsDAO;

import java.io.Serializable;


/**
 * Credential store API implementation.
 */
public class CredentialStoreImpl implements CredentialStore, Serializable {

    private CommunityUserDAO communityUserDAO;
    private CredentialsDAO credentialsDAO;

    public CredentialStoreImpl(DBUtil dbUtil) {

        this.communityUserDAO = new CommunityUserDAO(dbUtil);
        this.credentialsDAO = new CredentialsDAO(dbUtil);
    }

    @Override
    public String getPortalUser(String gatewayName, String communityUser) throws CredentialStoreException {
        CertificateCredential certificateCredential
                = this.credentialsDAO.getCredential(gatewayName, communityUser);
        return certificateCredential.getPortalUserName();
    }

    @Override
    public AuditInfo getAuditInfo(String gatewayName, String communityUser)
            throws CredentialStoreException {

        CertificateCredential certificateCredential
                = this.credentialsDAO.getCredential(gatewayName, communityUser);

        AuditInfo auditInfo = new AuditInfo();

        CommunityUser retrievedUser = certificateCredential.getCommunityUser();
        auditInfo.setCommunityUserName(retrievedUser.getUserName());
        auditInfo.setCredentialLifeTime(certificateCredential.getLifeTime());
        auditInfo.setCredentialsRequestedTime(certificateCredential.getCertificateRequestedTime());
        auditInfo.setGatewayName(gatewayName);
        auditInfo.setNotAfter(certificateCredential.getNotAfter());
        auditInfo.setNotBefore(certificateCredential.getNotBefore());
        auditInfo.setPortalUserName(certificateCredential.getPortalUserName());

        return auditInfo;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateCommunityUserEmail(String gatewayName, String communityUser, String email) throws CredentialStoreException {
        this.communityUserDAO.updateCommunityUser(
                new CommunityUser(gatewayName, communityUser, email));
    }

    @Override
    public void removeCredentials(String gatewayName, String communityUser) throws CredentialStoreException {
        credentialsDAO.deleteCredentials(gatewayName, communityUser);
    }



}
