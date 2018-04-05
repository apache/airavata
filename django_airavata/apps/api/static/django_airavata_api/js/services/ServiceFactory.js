export class ServiceFactory{
    constructor(serviceConfiguration){
        this.serviceConfiguration=serviceConfiguration;
    }

    service(serviceName){
        if(!serviceName){
            throw new TypeError("Invalid Service Name");
        }else if(!(serviceName in this.serviceConfiguration)){
            throw new Error("Service :"+serviceName+" could not be found");
        }

    }


}