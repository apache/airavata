/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.airavata.allocation.manager.client;

import java.util.List;
import org.apache.airavata.allocation.manager.models.*;
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

            transport = new TSocket("localhost", 9040);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
//            AllocationRegistryService.Client client = new AllocationRegistryService.Client(protocol);
//            System.out.println("Started client");
//
//          
//            
//            System.out.println("Testing createAllocationRequest() ");
//
//            UserAllocationDetail requestDetails = new UserAllocationDetail();
//            requestDetails.setProjectId("123456");
//            requestDetails.setUsername("harsha");
//            requestDetails.setRequestedDate(101L);
//            requestDetails.setTitle("Test");
//            requestDetails.setProjectDescription("Test");
//            requestDetails.setTypeOfAllocation("Comm");
//            requestDetails.setStatus("pending");
//
//            System.out.println(client.createAllocationRequest(requestDetails));
//
//            UserAllocationDetail rDetails1 = new UserAllocationDetail();
//            rDetails1.setProjectId("1234");
//            rDetails1.setUsername("madrina");
//            rDetails1.setRequestedDate(101L);
//            rDetails1.setTitle("Test");
//            rDetails1.setProjectDescription("Test");
//            rDetails1.setTypeOfAllocation("Comm");
//            rDetails1.setStatus("pending");
//
//            System.out.println(client.createAllocationRequest(rDetails1));
//            
//            System.out.println("######################");
//            
//            System.out.println("Testing deleteAllocationRequest() ");
//
//            System.out.println(client.deleteAllocationRequest("123456"));
//            
//            System.out.println("######################");
//            
//            System.out.println("Testing getAllocationRequest() ");
//
//            UserAllocationDetail userAllocationDetail = client.getAllocationRequest("123456");
//            System.out.println("Successful" + userAllocationDetail.getProjectDescription());
//            
//            System.out.println("######################");
//
//            System.out.println("Testing updateAllocationRequest() ");
//
//            UserAllocationDetail reqDetailsObj = new UserAllocationDetail();
//            reqDetailsObj.setProjectId("123456");
//            reqDetailsObj.setTitle("Title Temp");
//
//            System.out.println(client.updateAllocationRequest(reqDetailsObj));
//            
//            System.out.println("######################");
//
//            System.out.println("Testing isAdmin() ");
//
//            System.out.println("Successful: " + client.isAdmin("admin"));
//
//            System.out.println("Testing isReviewer() ");
//
//            System.out.println("Successful: " + client.isReviewer("reviewer1"));
//            
//            System.out.println("######################");
//
//            System.out.println("Testing getAllRequestsForAdmin() ");
//
//            List<UserAllocationDetail> userAllocationDetailList = client.getAllRequestsForAdmin("admin");
//            for (UserAllocationDetail object : userAllocationDetailList) {
//                System.out.println(object.getProjectId());
//            }
//            
//            System.out.println("######################");
//
//            System.out.println("Testing assignReviewers() ");
//
//            System.out.println(client.assignReviewers("123456", "reviewer1", "admin"));
//            System.out.println(client.assignReviewers("123456", "reviewer2", "admin"));
//            System.out.println(client.assignReviewers("1234", "reviewer2", "admin"));
//            System.out.println(client.assignReviewers("1234", "reviewer3", "admin"));
//            
//            System.out.println("######################");
//
//            System.out.println("Testing getAllRequestsForReviewers() ");
//
//            List<UserAllocationDetail> userAllocationDetailList1 = client.getAllRequestsForReviewers("reviewer2");
//            for (UserAllocationDetail object : userAllocationDetailList1) {
//                System.out.println(object.getProjectId());
//            }
//            
//            System.out.println("######################");
//
//            System.out.println("Testing getUserDetails() ");
//
//            UserDetail userDetail = client.getUserDetails("admin");
//            System.out.println(userDetail.getEmail());
//            
//            System.out.println("######################");
//
//            System.out.println("Testing updateRequestByReviewer() ");
//            ReviewerAllocationDetail reviewerAllocationDetail = new ReviewerAllocationDetail();
//            reviewerAllocationDetail.setProjectId("123456");
//            reviewerAllocationDetail.setUsername("reviewer2");
//            reviewerAllocationDetail.setMaxMemoryPerCpu(15L);
//            System.out.println(client.updateRequestByReviewer(reviewerAllocationDetail));
//
//            ReviewerAllocationDetail reviewerAllocationDetail1 = new ReviewerAllocationDetail();
//            reviewerAllocationDetail1.setProjectId("123456");
//            reviewerAllocationDetail1.setUsername("reviewer1");
//            reviewerAllocationDetail1.setMaxMemoryPerCpu(5L);
//            System.out.println(client.updateRequestByReviewer(reviewerAllocationDetail1));
//            
//            System.out.println("######################");
//
//            System.out.println("Testing getAllReviewsForARequest() ");
//
//            List<ReviewerAllocationDetail> reviewerAllocationDetailList = client.getAllReviewsForARequest("123456");
//            for (ReviewerAllocationDetail object : reviewerAllocationDetailList) {
//                System.out.println(object.getMaxMemoryPerCpu());
//            }
//            
//            System.out.println("######################");
//
//            System.out.println("Testing getAllUnassignedReviewersForRequest() ");
//
//            List<UserDetail> userDetailList = client.getAllUnassignedReviewersForRequest("123456");
//            for (UserDetail object : userDetailList) {
//                System.out.println(object.getUsername());
//            }
//            
//            System.out.println("######################");
//
//            System.out.println("Testing approveRequest() ");
//
//            System.out.println(client.approveRequest("123456", "admin", 1l, 2l, 50l));
//            
//            System.out.println("######################");
//
//            System.out.println("Testing rejectRequest() ");
//
//            System.out.println(client.rejectRequest("1234", "admin"));
//            
//            System.out.println("######################");

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
