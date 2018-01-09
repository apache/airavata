
import Experiment from '../models/Experiment'
import FetchUtils from '../utils/FetchUtils'

class ExperimentService {
    list(data = null) {
        if (data) {
            return Promise.resolve(data.map(result => new Experiment(result)));
        } else {
            return FetchUtils.get('/api/experiments/')
                .then(results => results.map(result => new Experiment(result)));
        }
    }

    create(experiment) {
        return FetchUtils.post('/api/experiments/', JSON.stringify(experiment))
            .then(result => new Experiment(result));
    }

    update() {
        // TODO
    }

    get() {
        // TODO
    }
}

// Export as a singleton
export default new ExperimentService();