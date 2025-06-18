import JobSubmissionService from "./JobSubmissionService";

class GlobusJobSubmissionService extends JobSubmissionService {
  constructor() {
    super();
    this.retrieveUrl = "/api/job/submission/globus";
  }
}

export default new GlobusJobSubmissionService();
