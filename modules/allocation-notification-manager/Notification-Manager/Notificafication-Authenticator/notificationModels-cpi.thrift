
namespace java org.apache.airavata.allocation.manager.notification.authenticator.stubs

include "./notificationModels.thrift"

service NotificationRequestDetailsService {
        list<notificationModels.Reviewer> getReviewers(1: required string requestID)
      
      	notificationModels.Admin getAdmin(1 : required string requestID)
      	
      	notificationModels.Request getStatus(1 : required string requestID )
 }