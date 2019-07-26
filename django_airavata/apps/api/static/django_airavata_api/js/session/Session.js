class Session {
  init({ username, airavataInternalUserId, isGatewayAdmin = false }) {
    this.username = username;
    this.airavataInternalUserId = airavataInternalUserId;
    this.isGatewayAdmin = isGatewayAdmin;
  }
}

export default new Session();
