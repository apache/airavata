namespace java com.request.Request  // defines the namespace
    
        typedef i32 int  //typedefs to get convenient names for your types

        struct Resource {
        1: i64 id,
        2: string content,
        3: string requestBy,
        4: string purpose
        }

    service RequestResourceService {  // defines the service to request resource
            string request(1:Resource res), //defines a method
    }