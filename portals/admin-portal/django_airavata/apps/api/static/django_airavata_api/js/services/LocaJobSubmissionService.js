import JobSubmissionService from "./JobSubmissionService";

class LocaJobSubmissionService extends JobSubmissionService {
  constructor() {
    super();
    this.retrieveUrl = "/api/job/submission/local";
  }
}

export default new LocaJobSubmissionService();
