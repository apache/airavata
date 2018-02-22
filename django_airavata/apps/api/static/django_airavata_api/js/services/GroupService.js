
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
}

// Export as a singleton
export default new GroupService();
