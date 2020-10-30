# airavata-ruby-sdk

Welcome to your new gem! In this directory, you'll find the files you need to be able to package up your Ruby library into a gem. Put your Ruby code in the file `lib/airavata/ruby/sdk`. To experiment with that code, run `bin/console` for an interactive prompt.

TODO: Delete this and the text above, and describe your gem

## Installation

Add this line to your application's Gemfile:

```ruby
gem 'airavata'
```

And then execute:

    $ bundle install

Or install it yourself as:

    $ gem install airavata

## Usage

### Basic Usage
```ruby
require "thrift"
require "airavata"
require_relative 'airavata/util/oauth'
require_relative 'airavata/util/transport'

api_client = get_airavata_api_client("localhost", "9930")

authz_token = get_authz_token("username", "password", "gatewayId")

api_client.isUserExists(authz_token, "gatewayId", "username")
```

### Import API host from rnvironment variables

.env
```
API_HOST = 'localhost'
API_PORT = 9930
```

```ruby
require "thrift"
require "airavata"
require_relative 'airavata/util/oauth'
require_relative 'airavata/util/transport'

api_client = get_airavata_api_client()

authz_token = get_authz_token("username", "password", "gatewayId")

api_client.isUserExists(authz_token, "gatewayId", "username")
```

## Development

After checking out the repo, run `bin/setup` to install dependencies. 

Then, run `bundle exec rspec` to run the tests. 

You can also run `bin/console` for an interactive prompt that will allow you to experiment.

To install this gem onto your local machine, run `bundle exec rake install`. 

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/apache/airavata/tree/master/airavata-api/airavata-client-sdks/airavata-ruby-sdk. This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the [code of conduct](https://github.com/apache/airavata/tree/master/airavata-api/airavata-client-sdks/airavata-ruby-sdk/CODE_OF_CONDUCT.md).

## Code of Conduct

Everyone interacting in the Airavata project's codebases, issue trackers, chat rooms and mailing lists is expected to follow the [code of conduct](https://github.com/apache/airavata/tree/master/airavata-api/airavata-client-sdks/airavata-ruby-sdk/CODE_OF_CONDUCT.md).

## Release

1. Setup credentials https://guides.rubygems.org/make-your-own-gem/
2. Bump the version in `airavata.gemspec`
3. Build the artifact `$ gem build airavata.gemspec`
5. Publish `$ gem push airavata-0.0.1.gem`
