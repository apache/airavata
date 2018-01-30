
import Group from '../models/Group'
import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'

class GroupService {

    listMemberGroups(data={}) {
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

    listOwnerGroups(data={}) {
      if (data && data.results) {
        return Promise.resolve(new PaginationIterator(data, Group));
      }
      else {
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

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new GroupService();
