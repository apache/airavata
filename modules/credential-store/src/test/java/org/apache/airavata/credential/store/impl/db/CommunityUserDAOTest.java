package org.apache.airavata.credential.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.CommunityUser;
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
                "                        COMMUNITY_USER_EMAIL VARCHAR(256) NOT NULL,\n" +
                "                        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)\n" +
                "                )";
        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        communityUserDAO = new CommunityUserDAO(getDbUtil());

        Connection connection = getDbUtil().getConnection();
        DBUtil.truncate("community_user", connection);

        connection.close();
    }

    @Test
    public void testAddCommunityUser() throws Exception {

        CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        communityUser = new CommunityUser("gw1", "ogce2","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce");
        Assert.assertNotNull(user);
        Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

        user = communityUserDAO.getCommunityUser("gw1", "ogce2");
        Assert.assertNotNull(user);
        Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());
    }

    @Test
    public void testDeleteCommunityUser() throws Exception {

        CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce");
        Assert.assertNotNull(user);

        communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.deleteCommunityUser(communityUser);

        user = communityUserDAO.getCommunityUser("gw1", "ogce");
        Assert.assertNull(user);

    }

    @Test
    public void testGetCommunityUsers() throws Exception {

        CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        CommunityUser user = communityUserDAO.getCommunityUser("gw1", "ogce");
        Assert.assertNotNull(user);
        Assert.assertEquals("ogce@sciencegateway.org", user.getUserEmail());

    }

    @Test
    public void testGetCommunityUsersForGateway() throws Exception {

        CommunityUser communityUser = new CommunityUser("gw1", "ogce","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        communityUser = new CommunityUser("gw1", "ogce2","ogce@sciencegateway.org");
        communityUserDAO.addCommunityUser(communityUser);

        List<CommunityUser> users = communityUserDAO.getCommunityUsers("gw1");
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());

        Assert.assertEquals(users.get(0).getUserName(), "ogce");
        Assert.assertEquals(users.get(1).getUserName(), "ogce2");
    }
}
