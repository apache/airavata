
import Project from '../models/Project'

class ProjectService {
    list(data = {}) {
        if (data && data.results) {
            let projects = data.results.map(result => new Project(result));
            this.next = data.next;
            this.previous = data.previous;
            return Promise.resolve({
                projects: projects,
                next: this.next,
                previous: this.previous
            });
        } else {
            return fetch('/api/projects', {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => {
                this.next = json.next;
                this.previous = json.previous;
                let projects = json.results.map(project => new Project(project));
                return {
                    projects: projects,
                    next: this.next,
                    previous: this.previous
                }
            });
        }
    }

    listNext() {
        // TODO
    }

    listPrevious() {
        // TODO
    }

    create() {
        // TODO
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