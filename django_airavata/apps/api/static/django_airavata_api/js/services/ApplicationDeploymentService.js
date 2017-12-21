
import ApplicationDeploymentDescription from '../models/ApplicationDeploymentDescription'
import BatchQueue from '../models/BatchQueue'

import FetchUtils from '../utils/FetchUtils'

class ApplicationDeploymentService {
    list(data = null) {
        if (data) {
            return Promise.resolve(data.map(result => new ApplicationDeploymentDescription(result)));
        } else {
            return FetchUtils.get('/api/application-deployments/')
                .then(json => json.map(result => new ApplicationDeploymentDescription(result)));
        }
    }

    create(project) {
        // TODO
    }

    update() {
        // TODO
    }

    get(appDeploymentId) {
        return FetchUtils.get('/api/application-deployments/' + encodeURIComponent(appDeploymentId) + '/')
            .then(json => new ApplicationDeploymentDescription(json))
    }

    getQueues(appDeploymentId) {
        return FetchUtils.get('/api/application-deployments/' + encodeURIComponent(appDeploymentId) + '/queues/')
            .then(json => json.map(result => new BatchQueue(result)));
    }
}

// Export as a singleton
export default new ApplicationDeploymentService();