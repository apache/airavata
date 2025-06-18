import JobSubmissionService from "./JobSubmissionService";

class CloudJobSubmissionService extends JobSubmissionService {
  constructor() {
    super();
    this.retrieveUrl = "/api/compute/resource/details";
  }
}

export default new CloudJobSubmissionService();
