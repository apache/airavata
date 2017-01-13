package org.apache.airavata.credential.store.credential;

/**
 * Created by marcus on 11/23/16.
 */
public enum CredentialOwnerType {
    GATEWAY(org.apache.airavata.model.credential.store.CredentialOwnerType.GATEWAY),
    USER(org.apache.airavata.model.credential.store.CredentialOwnerType.USER);

    private org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType;
    private CredentialOwnerType(org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType) {
       this.datamodelType = datamodelType;
    }

    public org.apache.airavata.model.credential.store.CredentialOwnerType getDatamodelType() {
        return datamodelType;
    }

    public static CredentialOwnerType findByDataModelType(org.apache.airavata.model.credential.store.CredentialOwnerType datamodelType) {
        for( CredentialOwnerType credentialOwnerType : CredentialOwnerType.values() ) {
            if (credentialOwnerType.datamodelType == datamodelType) {
                return credentialOwnerType;
            }
        }

        throw new RuntimeException("No CredentialOwnerType found for data model CredentialOwnerType " + datamodelType);
    }
}
