/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Model class representing the resolved/effective compute resource preference.
 *
 * <p>This is NOT a JPA entity - it's a plain model class used to hold the merged result
 * of preferences from multiple levels (GATEWAY, GROUP, USER). The resolution follows
 * the priority order: USER > GROUP > GATEWAY, where more specific levels override
 * less specific ones.
 *
 * <p>Each field tracks which level it was sourced from for debugging and audit purposes.
 * Use {@link #getFieldSourceLevel(String)} to determine where a specific field value came from.
 *
 * <p>Example usage:
 * <pre>{@code
 * EffectiveComputeResourcePreference effective = resolver.resolve(
 *     gatewayId, groupResourceProfileId, userId, computeResourceId);
 *
 * // Get the effective value
 * String loginUser = effective.getLoginUserName();
 *
 * // Check which level it came from
 * PreferenceLevel source = effective.getFieldSourceLevel("loginUserName");
 * // Returns USER if user had a preference, otherwise GROUP, otherwise GATEWAY
 * }</pre>
 *
 * @see PreferenceLevel
 */
public class EffectiveComputeResourcePreference {

    private String computeResourceId;

    // Core preference fields
    private String allocationProjectNumber;
    private String loginUserName;
    private Boolean overridebyAiravata;
    private String preferredBatchQueue;
    private DataMovementProtocol preferredDataMovementProtocol;
    private JobSubmissionProtocol preferredJobSubmissionProtocol;
    private String qualityOfService;
    private String reservation;
    private Timestamp reservationEndTime;
    private Timestamp reservationStartTime;
    private String resourceSpecificCredentialStoreToken;
    private String scratchLocation;

    // Gateway-level specific fields
    private String usageReportingGatewayId;
    private String sshAccountProvisioner;
    private String sshAccountProvisionerAdditionalInfo;

    // User-level specific fields
    private Boolean validated;

    /**
     * Tracks the source level for each field (for audit/debugging).
     * Key is the field name, value is the level it came from.
     */
    private final Map<String, PreferenceLevel> fieldSources = new java.util.HashMap<>();

    public EffectiveComputeResourcePreference() {}

    public EffectiveComputeResourcePreference(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    // ============================================
    // FIELD SOURCE TRACKING
    // ============================================

    /**
     * Set a field value with its source level tracking.
     *
     * @param fieldName the name of the field
     * @param level the level from which this field value originated
     */
    public void setFieldSourceLevel(String fieldName, PreferenceLevel level) {
        if (level != null) {
            fieldSources.put(fieldName, level);
        }
    }

    /**
     * Get the source level for a specific field.
     *
     * @param fieldName the name of the field
     * @return the PreferenceLevel from which this field's value originated, or null if not set
     */
    public PreferenceLevel getFieldSourceLevel(String fieldName) {
        return fieldSources.get(fieldName);
    }

    /**
     * Get all field source levels for debugging/audit.
     *
     * @return a copy of the field source map
     */
    public Map<String, PreferenceLevel> getAllFieldSourceLevels() {
        return new java.util.HashMap<>(fieldSources);
    }

    /**
     * Returns a human-readable summary of where each field value came from.
     * Useful for debugging preference resolution issues.
     *
     * @return a formatted string showing field sources
     */
    public String getFieldSourcesSummary() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Field Sources for computeResourceId=" + computeResourceId + ":");

        addFieldSource(sj, "allocationProjectNumber", allocationProjectNumber);
        addFieldSource(sj, "loginUserName", loginUserName);
        addFieldSource(sj, "overridebyAiravata", overridebyAiravata);
        addFieldSource(sj, "preferredBatchQueue", preferredBatchQueue);
        addFieldSource(sj, "preferredDataMovementProtocol", preferredDataMovementProtocol);
        addFieldSource(sj, "preferredJobSubmissionProtocol", preferredJobSubmissionProtocol);
        addFieldSource(sj, "qualityOfService", qualityOfService);
        addFieldSource(sj, "reservation", reservation);
        addFieldSource(sj, "reservationEndTime", reservationEndTime);
        addFieldSource(sj, "reservationStartTime", reservationStartTime);
        addFieldSource(sj, "resourceSpecificCredentialStoreToken", resourceSpecificCredentialStoreToken);
        addFieldSource(sj, "scratchLocation", scratchLocation);
        addFieldSource(sj, "usageReportingGatewayId", usageReportingGatewayId);
        addFieldSource(sj, "sshAccountProvisioner", sshAccountProvisioner);
        addFieldSource(sj, "sshAccountProvisionerAdditionalInfo", sshAccountProvisionerAdditionalInfo);
        addFieldSource(sj, "validated", validated);

        return sj.toString();
    }

    private void addFieldSource(StringJoiner sj, String fieldName, Object value) {
        PreferenceLevel source = fieldSources.get(fieldName);
        String valueStr = value != null ? value.toString() : "(null)";
        String sourceStr = source != null ? source.name() : "(not set)";
        sj.add(String.format("  %s: %s [from %s]", fieldName, valueStr, sourceStr));
    }

    // ============================================
    // CONVENIENCE METHODS FOR SETTING WITH TRACKING
    // ============================================

    /**
     * Set allocationProjectNumber with source tracking.
     */
    public void setAllocationProjectNumber(String value, PreferenceLevel source) {
        this.allocationProjectNumber = value;
        setFieldSourceLevel("allocationProjectNumber", source);
    }

    /**
     * Set loginUserName with source tracking.
     */
    public void setLoginUserName(String value, PreferenceLevel source) {
        this.loginUserName = value;
        setFieldSourceLevel("loginUserName", source);
    }

    /**
     * Set overridebyAiravata with source tracking.
     */
    public void setOverridebyAiravata(Boolean value, PreferenceLevel source) {
        this.overridebyAiravata = value;
        setFieldSourceLevel("overridebyAiravata", source);
    }

    /**
     * Set preferredBatchQueue with source tracking.
     */
    public void setPreferredBatchQueue(String value, PreferenceLevel source) {
        this.preferredBatchQueue = value;
        setFieldSourceLevel("preferredBatchQueue", source);
    }

    /**
     * Set preferredDataMovementProtocol with source tracking.
     */
    public void setPreferredDataMovementProtocol(DataMovementProtocol value, PreferenceLevel source) {
        this.preferredDataMovementProtocol = value;
        setFieldSourceLevel("preferredDataMovementProtocol", source);
    }

    /**
     * Set preferredJobSubmissionProtocol with source tracking.
     */
    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol value, PreferenceLevel source) {
        this.preferredJobSubmissionProtocol = value;
        setFieldSourceLevel("preferredJobSubmissionProtocol", source);
    }

    /**
     * Set qualityOfService with source tracking.
     */
    public void setQualityOfService(String value, PreferenceLevel source) {
        this.qualityOfService = value;
        setFieldSourceLevel("qualityOfService", source);
    }

    /**
     * Set reservation with source tracking.
     */
    public void setReservation(String value, PreferenceLevel source) {
        this.reservation = value;
        setFieldSourceLevel("reservation", source);
    }

    /**
     * Set reservationEndTime with source tracking.
     */
    public void setReservationEndTime(Timestamp value, PreferenceLevel source) {
        this.reservationEndTime = value;
        setFieldSourceLevel("reservationEndTime", source);
    }

    /**
     * Set reservationStartTime with source tracking.
     */
    public void setReservationStartTime(Timestamp value, PreferenceLevel source) {
        this.reservationStartTime = value;
        setFieldSourceLevel("reservationStartTime", source);
    }

    /**
     * Set resourceSpecificCredentialStoreToken with source tracking.
     */
    public void setResourceSpecificCredentialStoreToken(String value, PreferenceLevel source) {
        this.resourceSpecificCredentialStoreToken = value;
        setFieldSourceLevel("resourceSpecificCredentialStoreToken", source);
    }

    /**
     * Set scratchLocation with source tracking.
     */
    public void setScratchLocation(String value, PreferenceLevel source) {
        this.scratchLocation = value;
        setFieldSourceLevel("scratchLocation", source);
    }

    /**
     * Set usageReportingGatewayId with source tracking.
     */
    public void setUsageReportingGatewayId(String value, PreferenceLevel source) {
        this.usageReportingGatewayId = value;
        setFieldSourceLevel("usageReportingGatewayId", source);
    }

    /**
     * Set sshAccountProvisioner with source tracking.
     */
    public void setSshAccountProvisioner(String value, PreferenceLevel source) {
        this.sshAccountProvisioner = value;
        setFieldSourceLevel("sshAccountProvisioner", source);
    }

    /**
     * Set sshAccountProvisionerAdditionalInfo with source tracking.
     */
    public void setSshAccountProvisionerAdditionalInfo(String value, PreferenceLevel source) {
        this.sshAccountProvisionerAdditionalInfo = value;
        setFieldSourceLevel("sshAccountProvisionerAdditionalInfo", source);
    }

    /**
     * Set validated with source tracking.
     */
    public void setValidated(Boolean value, PreferenceLevel source) {
        this.validated = value;
        setFieldSourceLevel("validated", source);
    }

    // ============================================
    // STANDARD GETTERS AND SETTERS
    // ============================================

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getAllocationProjectNumber() {
        return allocationProjectNumber;
    }

    public void setAllocationProjectNumber(String allocationProjectNumber) {
        this.allocationProjectNumber = allocationProjectNumber;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public Boolean getOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(Boolean overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(DataMovementProtocol preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    public String getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
    }

    public Timestamp getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(Timestamp reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public Timestamp getReservationStartTime() {
        return reservationStartTime;
    }

    public void setReservationStartTime(Timestamp reservationStartTime) {
        this.reservationStartTime = reservationStartTime;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public String getUsageReportingGatewayId() {
        return usageReportingGatewayId;
    }

    public void setUsageReportingGatewayId(String usageReportingGatewayId) {
        this.usageReportingGatewayId = usageReportingGatewayId;
    }

    public String getSshAccountProvisioner() {
        return sshAccountProvisioner;
    }

    public void setSshAccountProvisioner(String sshAccountProvisioner) {
        this.sshAccountProvisioner = sshAccountProvisioner;
    }

    public String getSshAccountProvisionerAdditionalInfo() {
        return sshAccountProvisionerAdditionalInfo;
    }

    public void setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
        this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EffectiveComputeResourcePreference that = (EffectiveComputeResourcePreference) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(allocationProjectNumber, that.allocationProjectNumber)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(overridebyAiravata, that.overridebyAiravata)
                && Objects.equals(preferredBatchQueue, that.preferredBatchQueue)
                && preferredDataMovementProtocol == that.preferredDataMovementProtocol
                && preferredJobSubmissionProtocol == that.preferredJobSubmissionProtocol
                && Objects.equals(qualityOfService, that.qualityOfService)
                && Objects.equals(reservation, that.reservation)
                && Objects.equals(reservationEndTime, that.reservationEndTime)
                && Objects.equals(reservationStartTime, that.reservationStartTime)
                && Objects.equals(resourceSpecificCredentialStoreToken, that.resourceSpecificCredentialStoreToken)
                && Objects.equals(scratchLocation, that.scratchLocation)
                && Objects.equals(usageReportingGatewayId, that.usageReportingGatewayId)
                && Objects.equals(sshAccountProvisioner, that.sshAccountProvisioner)
                && Objects.equals(sshAccountProvisionerAdditionalInfo, that.sshAccountProvisionerAdditionalInfo)
                && Objects.equals(validated, that.validated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                computeResourceId,
                allocationProjectNumber,
                loginUserName,
                overridebyAiravata,
                preferredBatchQueue,
                preferredDataMovementProtocol,
                preferredJobSubmissionProtocol,
                qualityOfService,
                reservation,
                reservationEndTime,
                reservationStartTime,
                resourceSpecificCredentialStoreToken,
                scratchLocation,
                usageReportingGatewayId,
                sshAccountProvisioner,
                sshAccountProvisionerAdditionalInfo,
                validated);
    }

    @Override
    public String toString() {
        return "EffectiveComputeResourcePreference{"
                + "computeResourceId='" + computeResourceId + '\''
                + ", loginUserName='" + loginUserName + '\''
                + ", preferredBatchQueue='" + preferredBatchQueue + '\''
                + ", scratchLocation='" + scratchLocation + '\''
                + '}';
    }
}
