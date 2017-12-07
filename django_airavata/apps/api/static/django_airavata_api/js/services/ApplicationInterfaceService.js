
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
}

// Export as a singleton
export default new ApplicationIterfaceService();
