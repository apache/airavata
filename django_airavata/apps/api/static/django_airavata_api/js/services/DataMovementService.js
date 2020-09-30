import FetchUtils from "../utils/FetchUtils";

export default class DataMovementService {
  constructor() {
    this.retrieveUrl = null;
  }

  retrieve(id) {
    return FetchUtils.get(this.retrieveUrl, { id: id });
  }
}
