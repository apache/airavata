class Session {
  init({ username, airavataInternalUserId, isGatewayAdmin = false }) {
    this.username = username;
    this.airavataInternalUserId = airavataInternalUserId;
    this.isGatewayAdmin = isGatewayAdmin;
  }
}

const session = new Session();
if (window.AiravataPortalSessionData) {
  // Initialize portal session object with data provided by base.html template
  session.init(window.AiravataPortalSessionData);
}

export default session;
