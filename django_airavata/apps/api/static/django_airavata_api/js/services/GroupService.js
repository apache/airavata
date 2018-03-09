
import Group from '../models/Group'
import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'

class GroupService {

    list(data={}) {
      if (data && data.results) {
          return Promise.resolve(new PaginationIterator(data, Group));
      } else {
          return fetch('/api/groups/', {
              credentials: 'include'
          })
          .then(response => response.json())
          .then(json => new PaginationIterator(json, Group));
      }
    }

    create(group) {
        return FetchUtils.post('/api/groups/', JSON.stringify(group))
            .then(result => new Group(result))
    }

    update(group) {
        return FetchUtils.put('/api/groups/' + encodeURIComponent(group.id) + '/', JSON.stringify(group))
            .then(result => new Group(result));
    }

    get(groupId, data = null) {
        if (data) {
            return Promise.resolve(new Group(data));
        } else {
            return FetchUtils.get('/api/groups/'
                    + encodeURIComponent(groupId) + '/')
                .then(result => new Group(result));
        }
    }

    delete(groupId) {
        return FetchUtils.delete('/api/groups/'
                                 + encodeURIComponent(groupId) + '/');
    }

    // adminIds is an array of sharing user ids, for example:
    //   ['user1@test-domain', 'user2@test-domain']
    addAdmins(groupId, adminIds) {
        return FetchUtils.post('/api/groups/'
                                 + encodeURIComponent(groupId) + '/add_admins/',
                               JSON.stringify(adminIds));
    }

    // adminIds is an array of sharing user ids, for example:
    //   ['user1@test-domain', 'user2@test-domain']
    removeAdmins(groupId, adminIds) {
        return FetchUtils.post('/api/groups/'
                                 + encodeURIComponent(groupId) + '/remove_admins/',
                               JSON.stringify(adminIds));
    }
}

// Export as a singleton
export default new GroupService();
