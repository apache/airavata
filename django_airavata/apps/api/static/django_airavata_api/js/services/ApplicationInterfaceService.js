
import ApplicationInterfaceDefinition from '../models/ApplicationInterfaceDefinition'
import FetchUtils from '../utils/FetchUtils'

class ApplicationIterfaceService {
    list(data = null) {
        // TODO
    }

    create(project) {
        // TODO
    }

    update() {
        // TODO
    }

    get(appInterfaceId) {
        // TODO
    }

    getForAppModuleId(appModuleId) {
        return FetchUtils.get('/api/applications/' + encodeURIComponent(appModuleId) + '/application_interface/')
            .then(json => new ApplicationInterfaceDefinition(json))
    }

    getComputeResources(appInterfaceId) {
        return FetchUtils.get('/api/application-interfaces/' + encodeURIComponent(appInterfaceId) + '/compute_resources');
    }
}

// Export as a singleton
export default new ApplicationIterfaceService();
