package org.airavata.appcatalog.cpi;

import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayProfile;

public interface GProfile {
    /**
     * This method will add a gateway profile
     * @param gatewayProfile gateway profile
     * @return gateway id
     */
    String addGatewayProfile (GatewayProfile gatewayProfile) throws AppCatalogException;

    /**
     * This method will update a gateway profile
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateGatewayProfile (String gatewayId, GatewayProfile updatedProfile) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @return
     */
    GatewayProfile getGatewayProfile (String gatewayId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @param hostId
     * @return
     */
    JobSubmissionProtocol getPreferedJobSubmissionProtocol (String gatewayId, String hostId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @param hostId
     * @return
     */
    DataMovementProtocol getPreferedDMProtocol (String gatewayId, String hostId) throws AppCatalogException;


    /**
     * This method will remove a gateway profile
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeGatewayProfile (String gatewayId) throws AppCatalogException;

    /**
     * This method will check whether gateway profile exists
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean isGatewayProfileExists (String gatewayId) throws AppCatalogException;

}
