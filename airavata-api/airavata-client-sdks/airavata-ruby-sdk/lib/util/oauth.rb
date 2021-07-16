require 'oauth2'
require 'security_model_types'

ENV['OAUTH_DEBUG'] = 'true'

def get_authz_token(username, password, gateway_id)
  client = OAuth2::Client.new(
      'pga',
      '5d2dc66a-f54e-4fa9-b78f-80d33aa862c1',
      :site => 'https://iamdev.scigap.org',
      :token_url => "/auth/realms/seagrid/protocol/openid-connect/token"
  )

  oauth_token = client.password.get_token(username, password)

  AuthzToken.new(
      :accessToken => oauth_token.token,
      :claimsMap => {
          userName: username,
          gatewayID: gateway_id
      }
  )
end
