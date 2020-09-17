import JobSubmissionService from "./JobSubmissionService";

class UnicoreJobSubmissionService extends JobSubmissionService {
  constructor() {
    super();
    this.retrieveUrl = "/api/job/submission/unicore";
  }
}

export default new UnicoreJobSubmissionService();
