package org.apache.airavata.service.profile.utils;

import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.custos.commons.model.security.UserInfo;
import org.apache.custos.profile.model.tenant.PasswordCredential;
import org.apache.custos.profile.model.user.*;
import org.apache.custos.profile.model.workspace.GatewayApprovalStatus;

import java.util.ArrayList;
import java.util.List;

public class ThriftCustosDataModelConversion {


    public static org.apache.custos.commons.model.security.AuthzToken getCustosAuthzToken(AuthzToken authzToken) {
        org.apache.custos.commons.model.security.AuthzToken custosAuthz = new org.apache.custos.commons.model.security.AuthzToken();
        custosAuthz.setAccessToken(authzToken.getAccessToken());
        custosAuthz.setClaimsMap(authzToken.getClaimsMap());
        return custosAuthz;
    }

    public static org.apache.custos.profile.model.workspace.Gateway getCustosGateway(Gateway gateway) {
        org.apache.custos.profile.model.workspace.Gateway custosGateway  = new org.apache.custos.profile.model.workspace.Gateway();
        custosGateway.setCustosInternalGatewayId(gateway.getAiravataInternalGatewayId());
        custosGateway.setDeclinedReason(gateway.getDeclinedReason());
        custosGateway.setDomain(gateway.getDomain());
        custosGateway.setEmailAddress(gateway.getEmailAddress());
        custosGateway.setGatewayAcronym(gateway.getGatewayAcronym());
        custosGateway.setGatewayAdminEmail(gateway.getGatewayAdminEmail());
        custosGateway.setGatewayAdminFirstName(gateway.getGatewayAdminFirstName());
        custosGateway.setGatewayAdminLastName(gateway.getGatewayAdminLastName());
        custosGateway.setGatewayApprovalStatus(GatewayApprovalStatus.findByValue(gateway.getGatewayApprovalStatus().getValue()));
        custosGateway.setGatewayId(gateway.getGatewayId());
        custosGateway.setGatewayName(gateway.getGatewayName());
        custosGateway.setGatewayPublicAbstract(gateway.getGatewayPublicAbstract());
        custosGateway.setGatewayURL(gateway.getGatewayURL());
        custosGateway.setIdentityServerPasswordToken(gateway.getIdentityServerPasswordToken());
        custosGateway.setIdentityServerUserName(gateway.getIdentityServerUserName());
        custosGateway.setOauthClientId(gateway.getOauthClientId());
        custosGateway.setOauthClientSecret(gateway.getOauthClientSecret());
        custosGateway.setRequestCreationTime(gateway.getRequestCreationTime());
        custosGateway.setReviewProposalDescription(gateway.getReviewProposalDescription());
        return custosGateway;
    }

    public static Gateway getGateway(org.apache.custos.profile.model.workspace.Gateway gateway) {
        Gateway airavataGateway = new Gateway();
        airavataGateway.setAiravataInternalGatewayId(gateway.getCustosInternalGatewayId());
        airavataGateway.setDeclinedReason(gateway.getDeclinedReason());
        airavataGateway.setDomain(gateway.getDomain());
        airavataGateway.setEmailAddress(gateway.getEmailAddress());
        airavataGateway.setGatewayAcronym(gateway.getGatewayAcronym());
        airavataGateway.setGatewayAdminEmail(gateway.getGatewayAdminEmail());
        airavataGateway.setGatewayAdminFirstName(gateway.getGatewayAdminFirstName());
        airavataGateway.setGatewayAdminLastName(gateway.getGatewayAdminLastName());
        airavataGateway.setGatewayApprovalStatus(org.apache.airavata.model.workspace.GatewayApprovalStatus.findByValue(gateway.getGatewayApprovalStatus().getValue()));
        airavataGateway.setGatewayId(gateway.getGatewayId());
        airavataGateway.setGatewayName(gateway.getGatewayName());
        airavataGateway.setGatewayPublicAbstract(gateway.getGatewayPublicAbstract());
        airavataGateway.setGatewayURL(gateway.getGatewayURL());
        airavataGateway.setIdentityServerPasswordToken(gateway.getIdentityServerPasswordToken());
        airavataGateway.setIdentityServerUserName(gateway.getIdentityServerUserName());
        airavataGateway.setOauthClientId(gateway.getOauthClientId());
        airavataGateway.setOauthClientSecret(gateway.getOauthClientSecret());
        airavataGateway.setRequestCreationTime(gateway.getRequestCreationTime());
        airavataGateway.setReviewProposalDescription(gateway.getReviewProposalDescription());
        return airavataGateway;
    }

    public static List<Gateway> getGateways(List<org.apache.custos.profile.model.workspace.Gateway> gateways) {
        List<Gateway> airavataGateways = new ArrayList<>();
        for(org.apache.custos.profile.model.workspace.Gateway gateway: gateways) {
            airavataGateways.add(getGateway(gateway));
        }
        return airavataGateways;
    }

    public static UserProfile getUserProfile(org.apache.custos.profile.model.user.UserProfile initializeUserProfile) {
        UserProfile userProfile = new UserProfile();
        userProfile.setValidUntil(initializeUserProfile.getValidUntil());
        userProfile.setLastAccessTime(initializeUserProfile.getLastAccessTime());
        userProfile.setCreationTime(initializeUserProfile.getCreationTime());
        userProfile.setState(Status.findByValue(initializeUserProfile.getState().getValue()));
        userProfile.setLastName(initializeUserProfile.getLastName());
        userProfile.setFirstName(initializeUserProfile.getFirstName());
        userProfile.setEmails(initializeUserProfile.getEmails());
        userProfile.setUserModelVersion(initializeUserProfile.getUserModelVersion());
        userProfile.setGatewayId(initializeUserProfile.getGatewayId());
        userProfile.setUserId(initializeUserProfile.getUserId());
        userProfile.setAiravataInternalUserId(initializeUserProfile.getCustosInternalUserId());
        userProfile.setComments(initializeUserProfile.getComments());
        userProfile.setCountry(initializeUserProfile.getCountry());
        userProfile.setGpgKey(initializeUserProfile.getGpgKey());
        userProfile.setHomeOrganization(initializeUserProfile.getHomeOrganization());
        userProfile.setLabeledURI(initializeUserProfile.getLabeledURI());
        userProfile.setMiddleName(initializeUserProfile.getMiddleName());
        userProfile.setNamePrefix(initializeUserProfile.getNamePrefix());
        userProfile.setNameSuffix(initializeUserProfile.getNameSuffix());
        userProfile.setNationality(initializeUserProfile.getNationality());
        userProfile.setNsfDemographics(getNSFDemographics(initializeUserProfile.getNsfDemographics()));
        userProfile.setOrcidId(initializeUserProfile.getOrcidId());
        userProfile.setOrginationAffiliation(initializeUserProfile.getOrginationAffiliation());
        userProfile.setPhones(initializeUserProfile.getPhones());
        userProfile.setTimeZone(initializeUserProfile.getTimeZone());
        return userProfile;
    }

    public static List<UserProfile> getUserProfiles(List<org.apache.custos.profile.model.user.UserProfile> allUserProfilesInGateway) {
        List<UserProfile> userProfiles = new ArrayList<>();
        for(org.apache.custos.profile.model.user.UserProfile userProfile: allUserProfilesInGateway) {
            userProfiles.add(getUserProfile(userProfile));
        }
        return userProfiles;
    }

    public static org.apache.custos.profile.model.user.UserProfile getCustosUserProfile(UserProfile userProfile) {
        org.apache.custos.profile.model.user.UserProfile custosUserProfile = new org.apache.custos.profile.model.user.UserProfile();
        custosUserProfile.setValidUntil(userProfile.getValidUntil());
        custosUserProfile.setLastAccessTime(userProfile.getLastAccessTime());
        custosUserProfile.setCreationTime(userProfile.getCreationTime());
        custosUserProfile.setState(org.apache.custos.profile.model.user.Status.findByValue(userProfile.getState().getValue()));
        custosUserProfile.setLastName(userProfile.getLastName());
        custosUserProfile.setFirstName(userProfile.getFirstName());
        custosUserProfile.setEmails(userProfile.getEmails());
        custosUserProfile.setUserModelVersion(userProfile.getUserModelVersion());
        custosUserProfile.setGatewayId(userProfile.getGatewayId());
        custosUserProfile.setUserId(userProfile.getUserId());
        custosUserProfile.setCustosInternalUserId(userProfile.getAiravataInternalUserId());
        custosUserProfile.setComments(userProfile.getComments());
        custosUserProfile.setCountry(userProfile.getCountry());
        custosUserProfile.setGpgKey(userProfile.getGpgKey());
        custosUserProfile.setHomeOrganization(userProfile.getHomeOrganization());
        custosUserProfile.setLabeledURI(userProfile.getLabeledURI());
        custosUserProfile.setMiddleName(userProfile.getMiddleName());
        custosUserProfile.setNamePrefix(userProfile.getNamePrefix());
        custosUserProfile.setNameSuffix(userProfile.getNameSuffix());
        custosUserProfile.setNationality(userProfile.getNationality());
        custosUserProfile.setNsfDemographics(getCustosNSFDemographics(userProfile.getNsfDemographics()));
        custosUserProfile.setOrcidId(userProfile.getOrcidId());
        custosUserProfile.setOrginationAffiliation(userProfile.getOrginationAffiliation());
        custosUserProfile.setPhones(userProfile.getPhones());
        custosUserProfile.setTimeZone(userProfile.getTimeZone());
        return custosUserProfile;
    }

    public static PasswordCredential getCustosPasswordCredentials(org.apache.airavata.model.credential.store.PasswordCredential tenantAdminPasswordCredential) {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setDescription(tenantAdminPasswordCredential.getDescription());
        passwordCredential.setGatewayId(tenantAdminPasswordCredential.getGatewayId());
        passwordCredential.setLoginUserName(tenantAdminPasswordCredential.getLoginUserName());
        passwordCredential.setPassword(tenantAdminPasswordCredential.getPassword());
        passwordCredential.setPersistedTime(tenantAdminPasswordCredential.getPersistedTime());
        passwordCredential.setPortalUserName(tenantAdminPasswordCredential.getPortalUserName());
        passwordCredential.setToken(tenantAdminPasswordCredential.getToken());
        return passwordCredential;
    }

    private static NSFDemographics getCustosNSFDemographics(org.apache.airavata.model.user.NSFDemographics airavataNsfDemographics) {
        NSFDemographics nsfDemographics = new NSFDemographics();
        nsfDemographics.setCustosInternalUserId(airavataNsfDemographics.getAiravataInternalUserId());
        nsfDemographics.setDisabilities(getCustosDisablitiyList(airavataNsfDemographics.getDisabilities()));
        nsfDemographics.setEthnicities(getCustosEthnicitiesList(airavataNsfDemographics.getEthnicities()));
        nsfDemographics.setGender(airavataNsfDemographics.getGender());
        nsfDemographics.setRaces(getCustosRacesList(airavataNsfDemographics.getRaces()));
        nsfDemographics.setUsCitizenship(USCitizenship.findByValue(airavataNsfDemographics.getUsCitizenship().getValue()));
        return nsfDemographics;
    }

    private static org.apache.airavata.model.user.NSFDemographics getNSFDemographics(NSFDemographics custosNsfDemographics) {
        org.apache.airavata.model.user.NSFDemographics nsfDemographics = new org.apache.airavata.model.user.NSFDemographics();
        nsfDemographics.setDisabilities(getDisablitiyList(custosNsfDemographics.getDisabilities()));
        nsfDemographics.setEthnicities(getEthnicitiesList(custosNsfDemographics.getEthnicities()));
        nsfDemographics.setGender(custosNsfDemographics.getGender());
        nsfDemographics.setRaces(getRacesList(custosNsfDemographics.getRaces()));
        nsfDemographics.setUsCitizenship(org.apache.airavata.model.user.USCitizenship.findByValue(custosNsfDemographics.getUsCitizenship().getValue()));
        return nsfDemographics;
    }

    private static List<disability> getCustosDisablitiyList(List<org.apache.airavata.model.user.disability> disabilities) {
        List<disability> returnObject = new ArrayList<>();
        for(org.apache.airavata.model.user.disability d: disabilities) {
            returnObject.add(disability.findByValue(d.getValue()));
        }
        return returnObject;
    }

    private static List<ethnicity> getCustosEthnicitiesList(List<org.apache.airavata.model.user.ethnicity> ethnicities) {
        List<ethnicity> returnObject = new ArrayList<>();
        for(org.apache.airavata.model.user.ethnicity d: ethnicities) {
            returnObject.add(ethnicity.findByValue(d.getValue()));
        }
        return returnObject;
    }

    private static List<race> getCustosRacesList(List<org.apache.airavata.model.user.race> races) {
        List<race> returnObject = new ArrayList<>();
        for(org.apache.airavata.model.user.race d: races) {
            returnObject.add(race.findByValue(d.getValue()));
        }
        return returnObject;
    }

    private static List<org.apache.airavata.model.user.disability> getDisablitiyList(List<disability> disabilities) {
        List<org.apache.airavata.model.user.disability> returnObject = new ArrayList<>();
        for(disability d: disabilities) {
            returnObject.add(org.apache.airavata.model.user.disability.findByValue(d.getValue()));
        }
        return returnObject;
    }

    private static List<org.apache.airavata.model.user.ethnicity> getEthnicitiesList(List<ethnicity> ethnicities) {
        List<org.apache.airavata.model.user.ethnicity> returnObject = new ArrayList<>();
        for(ethnicity d: ethnicities) {
            returnObject.add(org.apache.airavata.model.user.ethnicity.findByValue(d.getValue()));
        }
        return returnObject;
    }

    private static List<org.apache.airavata.model.user.race> getRacesList(List<race> races) {
        List<org.apache.airavata.model.user.race> returnObject = new ArrayList<>();
        for(race d: races) {
            returnObject.add(org.apache.airavata.model.user.race.findByValue(d.getValue()));
        }
        return returnObject;
    }
}
