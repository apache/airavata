
import GroupMember from '../models/GroupMember'
import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'

class GroupMemberService {
    list(data = {}) {
        if (data && data.results) {
            return Promise.resolve(new PaginationIterator(data, GroupMember));
        } else {
            return fetch('/api/groups/', {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => new PaginationIterator(json, GroupMember));
        }
    }

    create(group) {
        return FetchUtils.post('/api/groups/', project.toJSONForCreate())
            .then(result => new GroupMember(result));
    }

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new GroupMemberService();
