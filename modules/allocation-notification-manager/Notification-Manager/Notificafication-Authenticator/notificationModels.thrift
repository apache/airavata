namespace java org.apache.airavata.allocation.manager.notification.authenticator.models
      
      
 struct Request {
    1: required string requestID,
    2: required string title,
    3: required string status,
    4: required string username,
    5: required string emailID
}

struct Reviewer{
    1: required string requestID,
    2: required string emailID,
}

struct Admin{
    1: required string requestID,
    2: required string emailID,
}