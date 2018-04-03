
import FetchUtils from '../utils/FetchUtils'
import GroupResourceProfile from '../models/GroupResourceProfile'

class GroupResourceProfileService {
    list(data = null) {
        if (data) {
            return Promise.resolve(data.map(result => new GroupResourceProfile(result)));
        } else {
            return FetchUtils.get('/api/group-resource-profiles/')
                .then(results => results.map(result => new GroupResourceProfile(result)));
        }
    }
}

// Export as a singleton
export default new GroupResourceProfileService();