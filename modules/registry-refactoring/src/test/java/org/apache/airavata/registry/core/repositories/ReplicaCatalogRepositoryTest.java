package org.apache.airavata.registry.core.repositories;

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.WorkflowEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.GatewayEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.NotificationEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.ProjectEntity;
import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
import org.apache.airavata.registry.core.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.core.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.core.repositories.workflowcatalog.WorkflowRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.NotificationRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.ProjectRepository;
import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by abhij on 10/14/2016.
 */
public class ReplicaCatalogRepositoryTest {

    private GatewayRepository gatewayRepository;
    private UserProfileRepository userProfileRepository;
    private String gatewayId;
    private String userId;
    private String dataProductUri;

    private final String NOTIFY_MESSAGE = "NotifyMe";
    private final String USER_COMMENT = "TestComment";
    private final String GATEWAY_DOMAIN = "test1.com";
    private final String DATA_PRODUCT_DESCRIPTION = "testDesc";


    @Before
    public void setupRepository()   {

        gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);


        gatewayId = "test.com" + System.currentTimeMillis();
        userId = "testuser" + System.currentTimeMillis();
        dataProductUri = "uri" + System.currentTimeMillis();

    }
    @Test
    public void dataProductRepositoryTest() {

        DataProductRepository dataProductRepository = new DataProductRepository(DataProductModel.class, DataProductEntity.class);

        /*
         * Creating Gateway required for UserProfile & Project creation
		 */
        Gateway gateway = new Gateway();
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
        gateway.setGatewayId(gatewayId);
        gateway.setDomain(GATEWAY_DOMAIN);
        gateway = gatewayRepository.create(gateway);
        Assert.assertTrue(!gateway.getGatewayId().isEmpty());

		/*
         * UserProfile Instance creation required for Project Creation
		 */
        UserProfile userProfile = new UserProfile();
        userProfile.setAiravataInternalUserId(userId);
        userProfile.setGatewayId(gateway.getGatewayId());
        userProfile = userProfileRepository.create(userProfile);
        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());

        /*
         * DataProduct Instance creation
         */
        DataProductModel dataProduct = new DataProductModel();
        dataProduct.setProductUri(dataProductUri);
        dataProduct.setGatewayId(gatewayId);
        dataProduct.setOwnerName(gatewayId);
        dataProduct.setProductName("Product1234");


        /*
         * Data Product Repository Insert Operation Test
		 */
        dataProduct = dataProductRepository.create(dataProduct);
        Assert.assertTrue(!dataProduct.getProductUri().isEmpty());



        /*
         * DataProduct Repository Update Operation Test
		 */
        dataProduct.setProductDescription(DATA_PRODUCT_DESCRIPTION);
        dataProductRepository.update(dataProduct);
        dataProduct = dataProductRepository.get(dataProduct.getProductUri());
        Assert.assertEquals(dataProduct.getProductDescription(), DATA_PRODUCT_DESCRIPTION);

		/*
         * Data Product Repository Select Operation Test
		 */
        dataProduct = null;
        dataProduct = dataProductRepository.get(dataProductUri);
        Assert.assertNotNull(dataProduct);

		/*
         * Workspace Project Repository Delete Operation
		 */
        boolean deleteResult = dataProductRepository.delete(dataProductUri);
        Assert.assertTrue(deleteResult);

        deleteResult = userProfileRepository.delete(userId);
        Assert.assertTrue(deleteResult);

        deleteResult = gatewayRepository.delete(gatewayId);
        Assert.assertTrue(deleteResult);

    }
}
