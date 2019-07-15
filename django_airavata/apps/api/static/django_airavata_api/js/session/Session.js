class Session {
  init({ username, airavataInternalUserId }) {
    this.username = username;
    this.airavataInternalUserId = airavataInternalUserId;
  }
}

export default new Session();
