package org.apache.airavata.sharing.registry.utils;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.sharing.registry.models.User;


/**
 * Created by Ajinkya on 3/29/17.
 */
public class ThriftDataModelConversion {

    /**
     * Build user object from UserProfile
     * @param userProfile thrift object
     * @return
     * User corresponding to userProfile thrift
     */
    public static User getUser(UserProfile userProfile){
        User user = new User();
        user.setUserId(userProfile.getUserId());
        user.setDomainId(userProfile.getGatewayId());
        user.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());
        user.setEmail(userProfile.getEmails().get(0));
        return user;
    }
}
