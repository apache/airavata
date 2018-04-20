/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.allocation.manager.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.airavata.allocation.manager.db.repositories.ProjectReviewerRepository;
//import org.apache.airavata.allocation.manager.db.repositories.Projec
import org.apache.airavata.allocation.manager.db.repositories.ReviewerAllocationDetailRepository;
import org.apache.airavata.allocation.manager.db.repositories.ReviewerSpecificResourceDetailRepository;
import org.apache.airavata.allocation.manager.db.repositories.UserAllocationDetailRepository;
import org.apache.airavata.allocation.manager.db.repositories.UserSpecificResourceDetailRespository;
import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.db.utils.JPAUtils;
import org.apache.airavata.allocation.manager.models.AllocationManagerException;
import org.apache.airavata.allocation.manager.models.ProjectReviewer;
import org.apache.airavata.allocation.manager.models.ReviewerAllocationDetail;
import org.apache.airavata.allocation.manager.models.ReviewerSpecificResourceDetail;
import org.apache.airavata.allocation.manager.models.UserAllocationDetail;
import org.apache.airavata.allocation.manager.models.UserSpecificResourceDetail;
import org.apache.airavata.allocation.manager.service.cpi.AllocationRegistryService;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationManagerServerHandler implements AllocationRegistryService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(AllocationManagerServerHandler.class);

    public static String OWNER_PERMISSION_NAME = "OWNER";

    public AllocationManagerServerHandler()
            throws AllocationManagerException, ApplicationSettingsException, TException, Exception {
        JPAUtils.initializeDB();
    }
    
    @Override
    //@SecurityCheck
    public long createAllocationRequest(AuthzToken authzToken, UserAllocationDetail allocDetail)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            if ((new UserAllocationDetailRepository()).isExists(allocDetail.getProjectId() + "")) {
                throw new TException("There exist project with the id");
            }
            
            allocDetail.setAllocationStatus(DBConstants.RequestStatus.PENDING);
            UserAllocationDetail create;
			create = (new UserAllocationDetailRepository()).create(allocDetail);
            return create.getProjectId();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public boolean deleteAllocationRequest(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            (new UserAllocationDetailRepository()).delete(projectId + "");
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    @SecurityCheck
    public UserAllocationDetail getAllocationRequest(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
        try {
            return (new UserAllocationDetailRepository().get(projectId + ""));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public boolean updateAllocationRequest(AuthzToken authzToken, UserAllocationDetail allocDetail)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            if ((new UserAllocationDetailRepository()).update(allocDetail).getProjectId() != 0l) {
                return true;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
        return false;
    }
    
    @Override
    @SecurityCheck
    public long createUserSpecificResource(AuthzToken authzToken, UserSpecificResourceDetail allocDetail)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            allocDetail.setSubStatus(DBConstants.RequestStatus.PENDING);
            UserSpecificResourceDetail create = (new UserSpecificResourceDetailRespository()).create(allocDetail);
            return allocDetail.getProjectId();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserSpecificResource(AuthzToken authzToken, long projectId, String specificResource)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    @SecurityCheck
    public List<UserSpecificResourceDetail> getUserSpecificResource(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            return (new UserSpecificResourceDetailRespository().getList(projectId));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public boolean updateUserSpecificResource(AuthzToken authzToken, long projectId,
            List<UserSpecificResourceDetail> listUserSpecificResource)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        for (UserSpecificResourceDetail userSpecificResourceDetail : listUserSpecificResource) {
            try {
                if (userSpecificResourceDetail.getId() == 0L) {
                    //create new
                    (new UserSpecificResourceDetailRespository()).create(userSpecificResourceDetail);
                } else {
                    (new UserSpecificResourceDetailRespository()).update(userSpecificResourceDetail);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }
    
    
    @Override
    @SecurityCheck
    public List<UserAllocationDetail> getAllRequests(AuthzToken authzToken, String userName, String userRole)
            throws AllocationManagerException, AuthorizationException, TException {
        List<UserAllocationDetail> userAllocationDetailList = new ArrayList<UserAllocationDetail>();
        try {
            userAllocationDetailList = new UserAllocationDetailRepository().getAllUserRequests(userName, userRole);
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
        return userAllocationDetailList;
    }

    @Override
    @SecurityCheck
    public boolean assignReviewers(AuthzToken authzToken, long projectId, String reviewerId, String adminId)
            throws AllocationManagerException, AuthorizationException, TException {
        try {
            ProjectReviewer projectReviewer = new ProjectReviewer();
            projectReviewer.setProjectId(projectId);
            projectReviewer.setReviewerUsername(reviewerId);

            ProjectReviewer projectReviewerObj = new ProjectReviewerRepository().create(projectReviewer);
            if (projectReviewerObj.getProjectId() != 0L) {
                // Update the status to under review.
                // Construct the primary key
                UserAllocationDetail userAllocationDetail = new UserAllocationDetailRepository().get(projectId + "")
                        .setAllocationStatus(DBConstants.RequestStatus.UNDER_REVIEW);
                // Updates the request
                new UserAllocationDetailRepository().update(userAllocationDetail);
            }
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
        return true;
    }
    
    @Override
    @SecurityCheck
    public boolean updateRequestByReviewer(AuthzToken authzToken, ReviewerAllocationDetail reviewerAllocationDetail)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            ReviewerAllocationDetail reviewerAllocationDetailObj = new ReviewerAllocationDetail();
            reviewerAllocationDetailObj = new ReviewerAllocationDetailRepository()
                    .isProjectExists(reviewerAllocationDetail.getProjectId() + "", reviewerAllocationDetail.getUsername());
            if (reviewerAllocationDetailObj != null) {
                reviewerAllocationDetail.setId(reviewerAllocationDetailObj.getId());
                reviewerAllocationDetailObj = (new ReviewerAllocationDetailRepository())
                        .update(reviewerAllocationDetail);
            } else {
                reviewerAllocationDetailObj = (new ReviewerAllocationDetailRepository())
                        .create(reviewerAllocationDetail);
            }
            if (reviewerAllocationDetailObj.getProjectId() != 0L) {
                return true;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
        return false;
    }
    
    @Override
    @SecurityCheck
    public List<UserAllocationDetail> getAllRequestsForReviewers(AuthzToken authzToken, String userName)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        List<UserAllocationDetail> userAllocationDetailList = new ArrayList<UserAllocationDetail>();
        try {
            List<ProjectReviewer> projReviewerList = (new ProjectReviewerRepository()).getProjectForReviewer(userName);
            List<String> projectIds = new ArrayList<String>();
            for (ProjectReviewer objProj : projReviewerList) {
                projectIds.add(objProj.getProjectId()+"");
            }
            return new UserAllocationDetailRepository().get(projectIds);
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public List<ReviewerAllocationDetail> getAllReviewsForARequest(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        List<ReviewerAllocationDetail> reviewerAllocationDetailList = new ArrayList<ReviewerAllocationDetail>();
        try {
            reviewerAllocationDetailList = new ReviewerAllocationDetailRepository().getAllReviewsForARequest(projectId);
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
        return reviewerAllocationDetailList;
    }
    
    @Override
    @SecurityCheck
    public long createReviewerSpecificResource(AuthzToken authzToken, ReviewerSpecificResourceDetail allocDetail)
            throws AllocationManagerException, AuthorizationException, TException {
        // TODO Auto-generated method stub
        try {
            ReviewerSpecificResourceDetail create = (new ReviewerSpecificResourceDetailRepository()).create(allocDetail);
            return allocDetail.getProjectId();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public boolean deleteReviewerSpecificResource(AuthzToken authzToken, long projectId, String specificResource)
            throws AllocationManagerException, AuthorizationException, TException {
        return false;
    }
    
    @Override
    @SecurityCheck
    public List<ReviewerSpecificResourceDetail> getReviewerSpecificResource(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
        try {
            return (new ReviewerSpecificResourceDetailRepository().getList(projectId));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }
    
    @Override
    @SecurityCheck
    public boolean updateReviewerSpecificResource(AuthzToken authzToken, long projectId,
            List<ReviewerSpecificResourceDetail> listReviewerSpecificResource)
            throws AllocationManagerException, AuthorizationException, TException {
        for (ReviewerSpecificResourceDetail reviewerSpecificResourceObj : listReviewerSpecificResource) {
            try {
            	ReviewerSpecificResourceDetail reviewerSpecificResourceDetail = (new ReviewerSpecificResourceDetailRepository()).getSpecificResource(projectId, reviewerSpecificResourceObj.specificResource,
                		reviewerSpecificResourceObj.username);
                if (reviewerSpecificResourceDetail == null) {
                    //create new
                    (new ReviewerSpecificResourceDetailRepository()).create(reviewerSpecificResourceObj);
                } else {
                    (new ReviewerSpecificResourceDetailRepository()).update(reviewerSpecificResourceObj.setId(reviewerSpecificResourceDetail.getId()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    @Override
    @SecurityCheck
    public List<ProjectReviewer> getAllAssignedReviewersForRequest(AuthzToken authzToken, long projectId)
            throws AllocationManagerException, AuthorizationException, TException {
	    	 try {
	    		 	return (new ProjectReviewerRepository().getList(projectId));
	         } catch (Exception ex) {
	             logger.error(ex.getMessage(), ex);
	             throw new AllocationManagerException()
	                     .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
	         }
    }
    
    @Override
    @SecurityCheck
    public boolean approveRequest(AuthzToken authzToken, long projectId, String adminId, long startDate, long endDate,
            long awardAllocation, String specificResourceName)
            throws AllocationManagerException, AuthorizationException, TException {
        try {
            // Create UserAllocationDetail object to call update method
            UserAllocationDetail userAllocDetail = getAllocationRequest(authzToken, projectId);
            UserSpecificResourceDetail userSpecificResourceDetail = new UserSpecificResourceDetail();
            userAllocDetail = new UserAllocationDetailRepository().get(projectId + "");
            userSpecificResourceDetail = new UserSpecificResourceDetailRespository().getSpecificResource(projectId, specificResourceName);
            if (userSpecificResourceDetail != null) {
	            userSpecificResourceDetail.setStartDate(startDate);
	            userSpecificResourceDetail.setEndDate(endDate);
	            userSpecificResourceDetail.setAllocatedServiceUnits(awardAllocation);
	            userSpecificResourceDetail.setSubStatus(DBConstants.RequestStatus.APPROVED);
	
	            (new UserSpecificResourceDetailRespository()).update(userSpecificResourceDetail);
	
	            List<UserSpecificResourceDetail> userSpecificResourceDetailList = getUserSpecificResource(authzToken, projectId);
	            boolean check = true;
	            for (UserSpecificResourceDetail userSpecificResourceDetail1 : userSpecificResourceDetailList) {
	                if (!userSpecificResourceDetail1.getSubStatus().equals(DBConstants.RequestStatus.APPROVED)) {
	                    check = false;
	                    break;
	                }
	            }
	            if (check) {
	                userAllocDetail.setAllocationStatus(DBConstants.RequestStatus.APPROVED);
	            } else {
	                userAllocDetail.setAllocationStatus(DBConstants.RequestStatus.PARTIALLYAPPROVED);
	            }
	            // updates the request
	            (new UserAllocationDetailRepository()).update(userAllocDetail);
	            return true;
            }
            return false;
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    @SecurityCheck
    public boolean rejectRequest(AuthzToken authzToken, long projectId, String adminId, String rejectionReason,
            String specificResourceName) throws AllocationManagerException, AuthorizationException, TException {
        try {

            // Create UserAllocationDetail object to call update method
            UserAllocationDetail userAllocDetail = getAllocationRequest(authzToken, projectId);
            UserSpecificResourceDetail userSpecificResourceDetail = new UserSpecificResourceDetail();
            userAllocDetail = new UserAllocationDetailRepository().get(projectId + "");
            userSpecificResourceDetail = new UserSpecificResourceDetailRespository().getSpecificResource(projectId, specificResourceName);
            userSpecificResourceDetail.setRejectionReason(rejectionReason);
            userSpecificResourceDetail.setSubStatus(DBConstants.RequestStatus.REJECTED);

            (new UserSpecificResourceDetailRespository()).update(userSpecificResourceDetail);

            List<UserSpecificResourceDetail> userSpecificResourceDetailList = getUserSpecificResource(authzToken, projectId);
            boolean check = true;
            for (UserSpecificResourceDetail userSpecificResourceDetail1 : userSpecificResourceDetailList) {
                if (!userSpecificResourceDetail1.getSubStatus().equals(DBConstants.RequestStatus.REJECTED)) {
                    check = false;
                    break;
                }
            }
            if (check) {
                userAllocDetail.setAllocationStatus(DBConstants.RequestStatus.REJECTED);
            }
            // updates the request
            (new UserAllocationDetailRepository()).update(userAllocDetail);
            return true;
        } catch (Exception ex) {
            throw new AllocationManagerException()
                    .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

	@Override
    @SecurityCheck
	public long getRemainingAllocationUnits(AuthzToken authzToken, String specificResource)
			throws AllocationManagerException, AuthorizationException, TException {
		//public static final String USER_NAME = "userName";
        //String username = authzToken.getClaimsMap().get("userName");
        String username="madrina";
        long projectId;
		try {
			projectId = (new UserAllocationDetailRepository()).getProjectId(username);
	        UserSpecificResourceDetail userSpecificResourceDetail = (new UserSpecificResourceDetailRespository()).getSpecificResource(projectId,specificResource);
	        if(userSpecificResourceDetail.getAllocatedServiceUnits()-userSpecificResourceDetail.getUsedServiceUnits() > 0)
	        		return userSpecificResourceDetail.getAllocatedServiceUnits()-userSpecificResourceDetail.getUsedServiceUnits();
	        else return 0l;
	}
        catch (Exception ex) {
        	 throw new AllocationManagerException()
             .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
		}
	}

	@Override
    @SecurityCheck
	public boolean deductAllocationUnits(AuthzToken authzToken, String specificResource, long allocationUnits)
			throws AllocationManagerException, AuthorizationException, TException {
			try {
				String username="madrina";
		        long projectId = (new UserAllocationDetailRepository()).getProjectId(username);
		        UserSpecificResourceDetail userSpecificResourceDetail = (new UserSpecificResourceDetailRespository()).getSpecificResource(projectId,specificResource);
		        long usedSU = userSpecificResourceDetail.getUsedServiceUnits() + allocationUnits;
		        userSpecificResourceDetail.setUsedServiceUnits(usedSU);
		        (new UserSpecificResourceDetailRespository()).update(userSpecificResourceDetail);
		        return true;
			}catch (Exception ex) {
	       	 throw new AllocationManagerException()
	            .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
			}
	}

	@Override
    @SecurityCheck
	public boolean canSubmitRequest(AuthzToken authzToken, String userName)
			throws AllocationManagerException, AuthorizationException, TException {
		
		boolean canSubmit = true;
		try {
			List<UserAllocationDetail> requestList = getAllRequests(authzToken, userName, "user");
			if(!requestList.isEmpty()) {
				Date requestedDate = new Date (requestList.get(0).getRequestedDate());
				long currentDate = System.currentTimeMillis();
				long diff = currentDate - requestList.get(0).getRequestedDate();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				
				if(diffDays < 365) {
					canSubmit = false;
				}
			}
		}catch (Exception ex) {
	       	 throw new AllocationManagerException()
	            .setMessage(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
			}
		return canSubmit;
	}
}
