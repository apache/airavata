namespace java org.apache.airavata.base.api
namespace php Airavata.Base.API
namespace cpp apache.airavata.base.api
namespace perl ApacheAiravataBaseAPI
namespace py airavata.base.api
namespace js ApacheAiravataBaseAPI

include "../airavata-apis/airavata_errors.thrift"

service BaseAPI {
    string getAPIVersion() throws (1: airavata_errors.AiravataSystemException ase)
}