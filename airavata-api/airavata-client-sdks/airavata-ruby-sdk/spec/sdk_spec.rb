require "thrift"
require 'oauth2'
require "dotenv"
require_relative '../lib/airavata'
require_relative '../lib/util/oauth'
require_relative '../lib/util/transport'

Dotenv.load('.env')

RSpec.describe Airavata do
  it "Test isUserExists" do
    api_client = get_airavata_api_client()
    authz_token = get_authz_token(ENV["TEST_USERNAME"], ENV["TEST_PASSWORD"], ENV["TEST_GATEWAY_ID"])

    expect(api_client.isUserExists(authz_token, ENV["TEST_GATEWAY_ID"], ENV["TEST_USERNAME"])).to eq(true)
  end
end
