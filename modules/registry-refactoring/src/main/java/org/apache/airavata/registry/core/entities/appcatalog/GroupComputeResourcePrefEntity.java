package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by skariyat on 2/8/18.
 */
@Entity
@Table(name = "GROUP_COMPUTE_RESOURCE_PREFERENCE")
@IdClass(GroupComputeResourcePrefPK.class)
public class GroupComputeResourcePrefEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "GATEWAY_ID")
    @Id
    private String gatewayID;

    @Column(name = "RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    @Id
    private String groupResourceProfileId;

    @Column(name = "ALLOCATION_PROJECT_NUMBER")
    private String allocationProjectNumber;

    @Column(name = "LOGIN_USERNAME")
    private String loginUserName;

    @Column(name = "OVERRIDE_BY_AIRAVATA")
    private short overridebyAiravata;

    @Column(name = "PREFERED_BATCH_QUEUE")
    private String preferredBatchQueue;

    @Column(name = "PREFERED_DATA_MOVE_PROTOCOL")
    private String preferredDataMovementProtocol;

    @Column(name = "PREFERED_JOB_SUB_PROTOCOL")
    private String preferredJobSubmissionProtocol;

    @Column(name = "QUALITY_OF_SERVICE")
    private String qualityOfService;

    @Column(name = "RESERVATION")
    private String reservation;

    @Column(name = "RESERVATION_END_TIME")
    private Timestamp reservationEndTime;

    @Column(name = "RESERVATION_START_TIME")
    private Timestamp reservationStartTime;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialStoreToken;

    @Column(name = "SCRATCH_LOCATION")
    private String scratchLocation;

    @Column(name = "USAGE_REPORTING_GATEWAY_ID")
    private String usageReportingGatewayId;

    @Column(name = "SSH_ACCOUNT_PROVISIONER")
    private String sshAccountProvisioner;

    @Column(name = "SSH_ACCOUNT_PROVISIONER_ADDITIONAL_INFO")
    private String sshAccountProvisionerAdditionalInfo;

    @OneToMany(targetEntity = GroupSSHAccountProvisionerConfig.class, mappedBy = "groupComputeResourcePref", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<GroupSSHAccountProvisionerConfig> groupSSHAccountProvisionerConfigs;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID"),
            @JoinColumn(name = "GATEWAY_ID")
    })
    private  GroupResourceProfileEntity groupResourceProfile;

    public GroupComputeResourcePrefEntity() {
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
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

    public short getOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(short overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
    }

    public String getPreferredBatchQueue() {
        return preferredBatchQueue;
    }

    public void setPreferredBatchQueue(String preferredBatchQueue) {
        this.preferredBatchQueue = preferredBatchQueue;
    }

    public String getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(String preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public String getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(String preferredJobSubmissionProtocol) {
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

    public List<GroupSSHAccountProvisionerConfig> getGroupSSHAccountProvisionerConfigs() {
        return groupSSHAccountProvisionerConfigs;
    }

    public void setGroupSSHAccountProvisionerConfigs(List<GroupSSHAccountProvisionerConfig> groupSSHAccountProvisionerConfigs) {
        this.groupSSHAccountProvisionerConfigs = groupSSHAccountProvisionerConfigs;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}
