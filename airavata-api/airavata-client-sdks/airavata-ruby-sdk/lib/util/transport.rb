require "thrift"
require 'airavata'

def get_airavata_api_client(host = "", port = "")
  if host == ""
    host = ENV["TEST_API_HOST"]
  end

  if port == ""
    port = ENV["TEST_API_PORT"]
  end

  transport = Thrift::BufferedTransport.new(Thrift::Socket.new(host, port))
  protocol = Thrift::BinaryProtocol.new(transport)
  api_client = Airavata::Client.new(protocol)
  transport.open()

  api_client
end
