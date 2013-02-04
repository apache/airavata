package org.apache.airavata.credential.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.CommunityUser;
import org.apache.airavata.credential.store.CredentialStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access class for community_user table.
 */
public class CommunityUserDAO extends ParentDAO {

    public CommunityUserDAO(DBUtil dbUtil) {
        super(dbUtil);
    }

    public void addCommunityUser(CommunityUser user) throws CredentialStoreException {

        String sql = "insert into community_user values (?, ?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getGatewayName());
            preparedStatement.setString(2, user.getUserName());
            preparedStatement.setString(3, user.getUserEmail());

            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community user.");
            stringBuilder.append("gateway - ").append(user.getGatewayName());
            stringBuilder.append("community user name - ").append(user.getUserName());
            stringBuilder.append("community user email - ").append(user.getUserEmail());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }
    }


    public void deleteCommunityUser(CommunityUser user) throws CredentialStoreException {

        String sql = "delete from community_user where gateway_name=? and community_user_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getGatewayName());
            preparedStatement.setString(2, user.getUserName());

            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error deleting community user.");
            stringBuilder.append("gateway - ").append(user.getGatewayName());
            stringBuilder.append("community user name - ").append(user.getUserName());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }
    }

    public void updateCommunityUser(CommunityUser user) throws CredentialStoreException {

        //TODO
    }

    public CommunityUser getCommunityUser(String gatewayName, String communityUserName) throws CredentialStoreException{

        String sql = "select * from community_user where gateway_name=? and community_user_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, communityUserName);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String email = resultSet.getString("COMMUNITY_USER_EMAIL");  //TODO fix typo

                return new CommunityUser(gatewayName, communityUserName, email);

            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving community user.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("community user name - ").append(communityUserName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }

        return null;
    }

    public List<CommunityUser> getCommunityUsers(String gatewayName)
            throws CredentialStoreException{

        List<CommunityUser> userList = new ArrayList<CommunityUser>();

        String sql = "select * from community_user where gateway_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String userName = resultSet.getString("COMMUNITY_USER_NAME");
                String email = resultSet.getString("COMMUNITY_USER_EMAIL");  //TODO fix typo

                userList.add(new CommunityUser(gatewayName, userName, email));

            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving community users for ");
            stringBuilder.append("gateway - ").append(gatewayName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }

        return userList;
    }


}
