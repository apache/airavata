import DataMovementService from "./DataMovementService";

class SCPDataMovementService extends DataMovementService {
  constructor() {
    super();
    this.retrieveUrl = "/api/data/movement/gridftp";
  }
}

export default new SCPDataMovementService();
