package org.apache.airavata.credential.store.credential;

/**
 * Created by marcus on 11/23/16.
 */
public enum CredentialOwnerType {
    GATEWAY(org.apache.airavata.credential.store.datamodel.CredentialOwnerType.GATEWAY),
    USER(org.apache.airavata.credential.store.datamodel.CredentialOwnerType.USER);

    private org.apache.airavata.credential.store.datamodel.CredentialOwnerType datamodelType;
    private CredentialOwnerType(org.apache.airavata.credential.store.datamodel.CredentialOwnerType datamodelType) {
       this.datamodelType = datamodelType;
    }

    public org.apache.airavata.credential.store.datamodel.CredentialOwnerType getDatamodelType() {
        return datamodelType;
    }

    public static CredentialOwnerType findByDataModelType(org.apache.airavata.credential.store.datamodel.CredentialOwnerType datamodelType) {
        for( CredentialOwnerType credentialOwnerType : CredentialOwnerType.values() ) {
            if (credentialOwnerType.datamodelType == datamodelType) {
                return credentialOwnerType;
            }
        }

        throw new RuntimeException("No CredentialOwnerType found for data model CredentialOwnerType " + datamodelType);
    }
}
