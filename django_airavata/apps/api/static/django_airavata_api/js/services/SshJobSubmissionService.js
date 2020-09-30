import JobSubmissionService from "./JobSubmissionService";

class SshJobSubmissionService extends JobSubmissionService {
  constructor() {
    super();
    this.retrieveUrl = "/api/job/submission/ssh";
  }
}

export default new SshJobSubmissionService();
