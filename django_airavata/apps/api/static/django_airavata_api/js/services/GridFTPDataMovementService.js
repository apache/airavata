import DataMovementService from "./DataMovementService";

class GridFTPDataMovementService extends DataMovementService {
  constructor() {
    super();
    this.retrieveUrl = "/api/data/movement/gridftp";
  }
}

export default new GridFTPDataMovementService();
