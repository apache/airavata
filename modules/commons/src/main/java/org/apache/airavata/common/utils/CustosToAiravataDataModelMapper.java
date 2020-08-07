package org.apache.airavata.common.utils;

import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.resource.secret.service.ResourceSecretType;
import org.apache.custos.resource.secret.service.SecretMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class responsible for map Airavarta data model to Custos data model
 */
public class CustosToAiravataDataModelMapper {
    private final static Logger logger = LoggerFactory.getLogger(CustosToAiravataDataModelMapper.class);


    public static UserProfile transform(UserRepresentation custosUserProfile, String gatewayId) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(custosUserProfile.getUsername());
        userProfile.setFirstName(custosUserProfile.getFirstName());
        userProfile.setAiravataInternalUserId(custosUserProfile.getUsername());
        userProfile.setLastName(custosUserProfile.getLastName());
        userProfile.setCreationTime(Double.valueOf(custosUserProfile.getCreationTime()).longValue());
        userProfile.setLastAccessTime(Double.valueOf(custosUserProfile.getLastLoginAt()).longValue());
        userProfile.setValidUntil(-1);
        List<String> emails = new ArrayList<>();
        emails.add(custosUserProfile.getEmail());
        userProfile.setEmails(emails);
        userProfile.setGatewayId(gatewayId);
        if (custosUserProfile.getState().equals(Status.ACTIVE.name())) {
            userProfile.setState(Status.ACTIVE);
        } else {
            userProfile.setState(Status.PENDING_CONFIRMATION);
        }
        return userProfile;

    }


    public static UserProfile transform(org.apache.custos.user.profile.service.UserProfile custosUserProfile, String gatewayId) throws ParseException {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(custosUserProfile.getUsername());
        userProfile.setFirstName(custosUserProfile.getFirstName());
        userProfile.setAiravataInternalUserId(custosUserProfile.getUsername());
        userProfile.setLastName(custosUserProfile.getLastName());
        userProfile.setCreationTime(custosUserProfile.getCreatedAt());
        userProfile.setLastAccessTime(custosUserProfile.getLastModifiedAt());
        userProfile.setValidUntil(-1);
        List<String> emails = new ArrayList<>();
        emails.add(custosUserProfile.getEmail());
        userProfile.setEmails(emails);
        userProfile.setGatewayId(gatewayId);
        if (custosUserProfile.getStatus().equals(Status.ACTIVE.name())) {
            userProfile.setState(Status.ACTIVE);
        } else {
            userProfile.setState(Status.PENDING_CONFIRMATION);
        }
        return userProfile;

    }

    public static CredentialSummary transform(SecretMetadata secretMetadata, String gatewayId) throws ParseException {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setDescription(secretMetadata.getDescription());
        credentialSummary.setGatewayId(gatewayId);
        credentialSummary.setToken(secretMetadata.getToken());
        credentialSummary.setPersistedTime(secretMetadata.getPersistedTime());
        credentialSummary.setUsername(secretMetadata.getOwnerId());

        if (secretMetadata.getType().equals(ResourceSecretType.PASSWORD)) {
            credentialSummary.setType(SummaryType.PASSWD);

        } else if (secretMetadata.getType().equals(ResourceSecretType.SSH)) {
            credentialSummary.setType(SummaryType.SSH);
        } else {
            credentialSummary.setType(SummaryType.CERT);
        }
        return credentialSummary;
    }

    public static SSHCredential transform(org.apache.custos.resource.secret.service.SSHCredential sshCredential,
                                          String gatewayId) {

        SSHCredential sshCredential1 = new SSHCredential();
        sshCredential1.setGatewayId(gatewayId);
        sshCredential1.setPrivateKey(sshCredential.getPrivateKey());
        sshCredential1.setPublicKey(sshCredential.getPublicKey());
        sshCredential1.setPassphrase(sshCredential.getPassphrase());
        sshCredential1.setPersistedTime(sshCredential.getMetadata().getPersistedTime());
        sshCredential1.setUsername(sshCredential.getMetadata().getOwnerId());
        sshCredential1.setDescription(sshCredential.getMetadata().getDescription());
        sshCredential1.setToken(sshCredential.getMetadata().getToken());

        return sshCredential1;

    }
}
