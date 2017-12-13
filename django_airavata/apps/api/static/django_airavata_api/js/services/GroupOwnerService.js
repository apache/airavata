
import GroupOwner from '../models/GroupOwner'
import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'

class GroupOwnerService {
    list(data = {}) {
        if (data && data.results) {
            return Promise.resolve(new PaginationIterator(data, GroupOwner));
        } else {
            return fetch('/api/groups/', {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => new PaginationIterator(json, GroupOwner));
        }
    }

    create(group) {
        return FetchUtils.post('/api/groups/', project.toJSONForCreate())
            .then(result => new GroupOwner(result));
    }

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new GroupOwnerService();
