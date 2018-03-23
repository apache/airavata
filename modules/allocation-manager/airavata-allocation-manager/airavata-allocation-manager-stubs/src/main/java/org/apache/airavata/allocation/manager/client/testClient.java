/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.airavata.allocation.manager.client;

import static java.util.Collections.list;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.allocation.manager.models.*;
import org.apache.airavata.allocation.manager.service.cpi.AllocationRegistryService;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class testClient {

    public static void main(String[] args) {

        try {
            TTransport transport;

            transport = new TSocket("localhost", 3010);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            AllocationRegistryService.Client client = new AllocationRegistryService.Client(protocol);
            System.out.println("Started client");

            
            AuthzToken token = new AuthzToken("empty_token");
            
            System.out.println("######################");
            System.out.println("Testing createAllocationRequest() 1");
            UserAllocationDetail requestDetails = new UserAllocationDetail();
            requestDetails.setUsername("harsha");
            requestDetails.setRequestedDate(101L);
            String title1 = new String("Test");
            requestDetails.setTitle(title1);
            requestDetails.setProjectDescription("Test");
            requestDetails.setAllocationStatus("PENDING");
            long pId1 = client.createAllocationRequest(token,requestDetails);
            System.out.println("First project Id: "+pId1);
            
            System.out.println("######################");
            System.out.println("Testing createAllocationRequest() 2");
            UserAllocationDetail requestDetails1 = new UserAllocationDetail();
            requestDetails1.setUsername("harsha2");
            requestDetails1.setRequestedDate(101L);
            requestDetails1.setTitle("Test2");
            requestDetails1.setProjectDescription("Test2");
            requestDetails1.setAllocationStatus("PENDING");
            long pId2 = client.createAllocationRequest(token,requestDetails1);
            System.out.println("Second project Id: "+pId2);
            
            System.out.println("######################");
            System.out.println("Testing deleteAllocationRequest");
            System.out.println(client.deleteAllocationRequest(token, pId2));
            
            System.out.println("######################");
            System.out.println("Testing getAllocationRequest() ");
            UserAllocationDetail userAllocationDetail = client.getAllocationRequest(token, pId1);
            System.out.println("Successful" + userAllocationDetail.getProjectDescription());
            
            
            System.out.println("######################");
            System.out.println("Testing updateAllocationRequest() ");
            UserAllocationDetail reqDetailsObj = new UserAllocationDetail();
            reqDetailsObj.setProjectId(pId1);
            reqDetailsObj.setTitle("Updated project title");
            System.out.println(client.updateAllocationRequest(token, reqDetailsObj));
            
            System.out.println("######################");
            System.out.println("Testing createUserSpecificResource 1");
            UserSpecificResourceDetail userSpecObj = new UserSpecificResourceDetail();
            userSpecObj.setProjectId(pId1);
            userSpecObj.setAllocatedServiceUnits(1500);
            userSpecObj.setApplicationsToBeUsed("Kafka");
            userSpecObj.setEndDate(12L);
            userSpecObj.setRequestedServiceUnits(1800);
            userSpecObj.setResourceType("gpu");
            userSpecObj.setSpecificResource("large gpu");
            userSpecObj.setSubStatus("PENDING");
            long userSpecId1 = client.createUserSpecificResource(token, userSpecObj);
            System.out.println("First user spec Id: "+userSpecId1);
            
            
            System.out.println("######################");
            System.out.println("Testing createUserSpecificResource 2");
            UserSpecificResourceDetail userSpecObj1 = new UserSpecificResourceDetail();
            userSpecObj1.setProjectId(pId1);
            userSpecObj1.setAllocatedServiceUnits(150);
            userSpecObj1.setApplicationsToBeUsed("Kafka test");
            userSpecObj1.setEndDate(12L);
            userSpecObj1.setRequestedServiceUnits(180);
            userSpecObj1.setResourceType("gpu test");
            userSpecObj1.setSpecificResource("large gpu test");
            userSpecObj1.setSubStatus("PENDING");
            long userSpecId2 = client.createUserSpecificResource(token, userSpecObj);
            System.out.println("Second user spec Id: "+userSpecId2); 
            
            System.out.println("######################");
            System.out.println("Testing getUserSpecificResource 1");
            List<UserSpecificResourceDetail> userSpecObjList;
            userSpecObjList = new ArrayList<UserSpecificResourceDetail>(2);
            List<UserSpecificResourceDetail> userUpdatedList;
            userUpdatedList = new ArrayList<UserSpecificResourceDetail>(2);
            userSpecObjList = client.getUserSpecificResource(token, pId1);
            for (UserSpecificResourceDetail obj:userSpecObjList)
            {
                System.out.println("Specific res: "+obj.specificResource+", res type: "+obj.resourceType);
                obj.setApplicationsToBeUsed("skyligght");
                userUpdatedList.add(obj);
            }
            
            System.out.println("######################");
            System.out.println("Testing updateUserSpecificResource");
            client.updateUserSpecificResource(token, pId1, userUpdatedList);
            
            System.out.println("######################");
            System.out.println("Testing getAllRequestsForAdmin");
            List<UserAllocationDetail> userAllocationDetailList = client.getAllRequestsForAdmin(token, "admin");
            for (UserAllocationDetail object : userAllocationDetailList) {
                System.out.println(object.getProjectId());
            }
            System.out.println("######################");
            System.out.println("Testing assignReviewers() ");
            System.out.println(client.assignReviewers(token, pId1, "reviewer1", "admin"));
            
            
            System.out.println("######################");
            System.out.println("Testing updateRequestByReviewer");
            ReviewerAllocationDetail reviewerAllocationDetail = new ReviewerAllocationDetail();
            reviewerAllocationDetail.setProjectId(pId1);
            reviewerAllocationDetail.setUsername("reviewer1");
            reviewerAllocationDetail.setMaxMemoryPerCpu(15L);
            reviewerAllocationDetail.setTypicalSuPerJob(121);
            System.out.println(client.updateRequestByReviewer(token, reviewerAllocationDetail));
            
            System.out.println("######################");
            System.out.println("Testing getAllRequestsForReviewers");
            List<UserAllocationDetail> userAllocationDetailList1 = client.getAllRequestsForReviewers(token, "reviewer1");
            for (UserAllocationDetail object : userAllocationDetailList1) {
                System.out.println(object.getProjectId());
            }
            System.out.println("######################");
            System.out.println("Testing getAllReviewsForARequest() ");
            List<ReviewerAllocationDetail> reviewerAllocationDetailList = client.getAllReviewsForARequest(token, pId1);
            for (ReviewerAllocationDetail object : reviewerAllocationDetailList) {
                System.out.println("MaxMemoryPerCpu: " + object.getMaxMemoryPerCpu());
            }
            
            System.out.println("######################");
            System.out.println("Testing createReviewerSpecificResource 1");
            ReviewerSpecificResourceDetail revSpecObj1 = new ReviewerSpecificResourceDetail();
            revSpecObj1.setProjectId(pId1);
            revSpecObj1.setApplicationsToBeUsed("Kafka");
            revSpecObj1.setResourceType("gpu");
            revSpecObj1.setSpecificResource("largest gpu");
            long revSpecId1 = client.createReviewerSpecificResource(token, revSpecObj1);
            System.out.println("First reviewer spec Id: "+ revSpecId1);
            
            
            System.out.println("######################");
            System.out.println("Testing createReviewerSpecificResource 2");
            ReviewerSpecificResourceDetail revSpecObj2 = new ReviewerSpecificResourceDetail();
            revSpecObj2.setProjectId(pId1);
            revSpecObj2.setApplicationsToBeUsed("Apcahe Kafka");
            revSpecObj2.setResourceType("cpu and gpu");
            revSpecObj2.setSpecificResource("largest gpu");
            long revSpecId2 = client.createReviewerSpecificResource(token, revSpecObj2);
            System.out.println("Second reviewer spec Id: "+ revSpecId2); 
            System.out.println("######################");
            System.out.println("Testing getReviewerSpecificResource 1");
            List<ReviewerSpecificResourceDetail> revSpecObjList;
            revSpecObjList = new ArrayList<ReviewerSpecificResourceDetail>(2);
            List<ReviewerSpecificResourceDetail> revUpdatedList;
            revUpdatedList = new ArrayList<ReviewerSpecificResourceDetail>(2);
            revSpecObjList = client.getReviewerSpecificResource(token, pId1);
            for (ReviewerSpecificResourceDetail obj1:revSpecObjList)
            {
                System.out.println("Specific res: "+obj1.specificResource+", res type: "+obj1.resourceType);
                obj1.setApplicationsToBeUsed("sky and grass");
                revUpdatedList.add(obj1);
            }
            
            System.out.println("######################");
            System.out.println("Testing updateReviewerSpecificResource");
            client.updateReviewerSpecificResource(token, pId1, revUpdatedList); 
            
            System.out.println("######################");
            System.out.println("Testing getAllAssignedReviewersForRequest");
            List<ProjectReviewer> userDetailList = client.getAllAssignedReviewersForRequest(token, pId1);
            for (ProjectReviewer object : userDetailList) {
                System.out.println(object.getReviewerUsername());
            }
            
            System.out.println("######################");
            System.out.println("Testing approveRequest");
            System.out.println(client.approveRequest(token, pId1, "admin", 1l, 2l, 50l, "large gpu"));
            
            
            System.out.println("######################");
            System.out.println("Testing rejectRequest() ");
            System.out.println(client.rejectRequest(token, 14, "admin", "rejecttest","large gpu"));
            
            System.out.println("######################");
            System.out.println("Testing getRemainingAllocationUnits");
            System.out.println(client.getRemainingAllocationUnits(token, "large gpu"));
            
            System.out.println("######################");
            System.out.println("Testing deductAllocationUnits");
            System.out.println(client.deductAllocationUnits(token, "large gpu", 10));
            
            System.out.println("End of testing!!!!");

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException x) {
            x.printStackTrace();
        } finally {
            //transport.close();
        }
    }

}
