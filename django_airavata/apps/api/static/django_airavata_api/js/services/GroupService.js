import Group from '../models/Group'
import FetchUtils from '../utils/FetchUtils'

class GroupService {

    create(group) {
        return FetchUtils.post('/api/groups/', group.toJSONForCreate())
            .then(result => new Group(result));
    }

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new GroupService();
