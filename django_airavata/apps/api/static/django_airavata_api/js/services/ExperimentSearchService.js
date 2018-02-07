
import ExperimentSummary from '../models/ExperimentSummary'
import FetchUtils from '../utils/FetchUtils'
import PaginationIterator from '../utils/PaginationIterator'

class ExperimentSearchService {
    list(data = {}) {
        if (data && data.results) {
            return Promise.resolve(new PaginationIterator(data, ExperimentSummary));
        } else {
            return fetch('/api/experiment-search/', {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(json => new PaginationIterator(json, ExperimentSummary));
        }
    }
}

// Export as a singleton
export default new ExperimentSearchService();