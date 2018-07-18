
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

    update(experiment) {
        return FetchUtils.put('/api/experiments/'
                + encodeURIComponent(experiment.experimentId) + '/',
                JSON.stringify(experiment))
            .then(result => new Experiment(result));
    }

    save(experiment) {
        if (experiment.experimentId) {
            return this.update(experiment);
        } else {
            return this.create(experiment);
        }
    }

    get(experimentId, data = null) {
        if (data) {
            return Promise.resolve(new Experiment(data));
        } else {
            return FetchUtils.get('/api/experiments/'
                    + encodeURIComponent(experimentId) + '/')
                .then(result => new Experiment(result));
        }
    }

    launch(experimentId) {
        return FetchUtils.post('/api/experiments/' + encodeURIComponent(experimentId) + '/launch/')
            .then(result => {
                if (result.success) {
                    return Promise.resolve(result);
                } else {
                    return Promise.reject(result);
                }
            });
    }
}

// Export as a singleton
export default new ExperimentService();