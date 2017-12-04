
import Project from '../models/Project'
import PaginationIterator from '../utils/PaginationIterator'
import FetchUtils from '../utils/FetchUtils'

class ProjectService {
    list(data = {}) {
        if (data && data.results) {
            return Promise.resolve(new PaginationIterator(data, Project));
        } else {
            return fetch('/api/projects/', {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => new PaginationIterator(json, Project));
        }
    }

    listAll() {
        return fetch('/api/projects/list_all/', {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(json => json.map(project => new Project(project)));
    }

    create(project) {
        return FetchUtils.post('/api/projects/', project.toJSONForCreate())
            .then(result => new Project(result));
    }

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new ProjectService();