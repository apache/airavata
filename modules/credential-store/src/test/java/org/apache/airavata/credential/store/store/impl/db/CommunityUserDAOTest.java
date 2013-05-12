package org.apache.airavata.credential.store.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.junit.*;

import java.sql.Connection;
import java.util.List;

/**
 * Test for community user DAO.
 */
public class CommunityUserDAOTest extends DatabaseTestCases {

    private CommunityUserDAO communityUserDAO;

    @BeforeClass
    public static void setUpDatabase() throws Exception{

        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        String createTable = "CREATE TABLE COMMUNITY_USER\n" +
                "                (\n" +
                "                        GATEWAY_NAME VARCHAR(256) NOT NULL,\n" +
                "                        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,\n" +
                "                        TOKEN_ID VARCHAR(256) NOT NULL,\n" +
                "                        COMMUNITY_USER_EMAIL VARCHAR(256) NOT NULL,\n" +
                "                        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME, TOKEN_ID)\n" +
                "                )";


        String dropTable = "drop table COMMUNITY_USER";

        try {
            executeSQL(dropTable);
        } catch (Exception e){}

        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        communityUserDAO = new CommunityUserDAO();

        Connection connection = getDbUtil().getConnection();

        try {
            DBUtil.truncate("community_user", connection);
        } finally {
            connection.close();
        }

    }

    @Test
    public void testAddCommunityUser() throws Exception {

        Connection connection = getConnection();

        try {

            CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.addCommunityUser(communityUser, "Token1", connection);

            communityUser = new CommunityUser("gw1", "ogce2","ogce@sciencegateway.org");
            communityUserDAO.addCommunityUser(communityUser, "Token2", connection);

            CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNotNull(user);
            Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

            user = communityUserDAO.getCommunityUser("gw1", "ogce2", connection);
            Assert.assertNotNull(user);
            Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

            user = communityUserDAO.getCommunityUserByToken("gw1", "Token1", connection);
            Assert.assertNotNull(user);
            Assert.assertEquals("ogce", user.getUserName());
            Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

            user = communityUserDAO.getCommunityUserByToken("gw1", "Token2", connection);
            Assert.assertNotNull(user);
            Assert.assertEquals("ogce2", user.getUserName());
            Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

        } finally {
            connection.close();
        }

    }

    @Test
    public void testDeleteCommunityUser() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.addCommunityUser(communityUser, "Token1", connection);

            CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNotNull(user);

            communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.deleteCommunityUser(communityUser, connection);

            user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNull(user);

        } finally {
            connection.close();
        }
    }

    @Test
    public void testDeleteCommunityUserByToken() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.addCommunityUser(communityUser, "Token1", connection);

            CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNotNull(user);

            communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.deleteCommunityUserByToken(communityUser, "Token1", connection);

            user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNull(user);

        } finally {
            connection.close();
        }


    }

    @Test
    public void testGetCommunityUsers() throws Exception {

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
            communityUserDAO.addCommunityUser(communityUser, "Token1", connection);

            CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce", connection);
            Assert.assertNotNull(user);
            Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

        } finally {
            connection.close();
        }


    }

    @Test
    public void testGetCommunityUsersForGateway() throws Exception {

        Connection connection = getConnection();

        CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser, "Token1", connection);

        communityUser = new CommunityUser("gw1", "ogce2","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser, "Token2", connection);

        List<CommunityUser> users = communityUserDAO.getCommunityUsers("gw1", connection);
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());

        Assert.assertEquals(users.get(0).getUserName(), "ogce");
        Assert.assertEquals(users.get(1).getUserName(), "ogce2");
    }
}
