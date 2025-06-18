import FullExperiment from "../models/FullExperiment";
import FetchUtils from "../utils/FetchUtils";

class FullExperimentService {
  get(experimentId, data = null) {
    if (data) {
      return Promise.resolve(new FullExperiment(data));
    } else {
      return FetchUtils.get(
        "/api/full-experiments/" + encodeURIComponent(experimentId) + "/"
      ).then((result) => new FullExperiment(result));
    }
  }
}

// Export as a singleton
export default new FullExperimentService();
