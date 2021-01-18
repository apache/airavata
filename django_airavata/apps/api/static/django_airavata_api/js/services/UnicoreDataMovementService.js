import DataMovementService from "./DataMovementService";

class UnicoreDataMovementService extends DataMovementService {
  constructor() {
    super();
    this.retrieveUrl = "/api/data/movement/gridftp";
  }
}

export default new UnicoreDataMovementService();
