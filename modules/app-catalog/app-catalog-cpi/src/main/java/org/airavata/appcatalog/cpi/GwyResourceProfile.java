package org.airavata.appcatalog.cpi;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;

import java.util.List;

public interface GwyResourceProfile {
    /**
     * This method will add a gateway profile
     * @param gatewayProfile gateway profile
     * @return gateway id
     */
    String addGatewayResourceProfile(GatewayResourceProfile gatewayProfile) throws AppCatalogException;

    /**
     * This method will update a gateway profile
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @return
     */
    org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile getGatewayProfile (String gatewayId) throws AppCatalogException;

    /**
     * This method will remove a gateway profile
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException;

    /**
     * This method will check whether gateway profile exists
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @param hostId
     * @return ComputeResourcePreference
     */
    ComputeResourcePreference getComputeResourcePreference (String gatewayId, String hostId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @return
     */
    List<ComputeResourcePreference> getAllComputeResourcePreferences (String gatewayId) throws AppCatalogException;
}
