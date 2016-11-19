package com.apache.airavata.user.profile.server;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.apache.airavata.userprofile.crude.cpi.UserProfileCrudeService;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Airavata on 11/11/2016.
 */
public class UserProfileHandler implements UserProfileCrudeService.Iface {

    private UserProfileRepository userProfileRepository;

    public UserProfileHandler() {

        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
    }

    public String addUserProfile(UserProfile userProfile) throws RegistryServiceException, TException {

        userProfileRepository.create(userProfile);

        if (null != userProfile)
            return userProfile.getUserId();

        return null;
    }

    public boolean updateUserProfile(UserProfile userProfile) throws RegistryServiceException, TException {

        try {
            userProfileRepository.update(userProfile);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    public UserProfile getUserProfileById(String userId, String gatewayId) throws RegistryServiceException, TException {


        UserProfile userProfile = userProfileRepository.getUserProfileByIdAndGateWay(userId, gatewayId);

        return userProfile;
    }

    public boolean deleteUserProfile(String userId) throws RegistryServiceException, TException {

        boolean deleteResult = userProfileRepository.delete(userId);

        return deleteResult;
    }

    public List<UserProfile> getAllUserProfilesInGateway(String gatewayId) throws RegistryServiceException, TException {

        List<UserProfile> usersInGateway = userProfileRepository.getAllUserProfilesInGateway(gatewayId);
        return usersInGateway;
    }

    public UserProfile getUserProfileByName(String userName, String gatewayId) throws RegistryServiceException, TException {

        UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);
        return userProfile;
    }

    public boolean doesUserExist(String userName, String gatewayId) throws RegistryServiceException, TException {

        UserProfile userProfile = userProfileRepository.getUserProfileByNameAndGateWay(userName, gatewayId);

                if(null != userProfile)
                    return true;
        return false;
    }
}
