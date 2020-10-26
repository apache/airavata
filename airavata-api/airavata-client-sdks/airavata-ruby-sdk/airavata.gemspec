Gem::Specification.new do |spec|
  spec.name = "airavata"
  spec.version = "0.0.1"
  spec.authors = ["Dinuka De Silva"]
  spec.email = ["L.dinukadesilva@gmail.com"]

  spec.summary = "Airavata Ruby SDK"
  spec.description = "The Apache Airavata Ruby SDK for third party clients to integrate with Airavata middleware"
  spec.homepage = "https://github.com/apache/airavata"
  spec.required_ruby_version = Gem::Requirement.new(">= 2.3.0")

  spec.metadata["allowed_push_host"] = "https://rubygems.org"

  spec.metadata["homepage_uri"] = spec.homepage
  spec.metadata["source_code_uri"] = spec.homepage
  spec.metadata["changelog_uri"] = spec.homepage

  # Specify which files should be added to the gem when it is released.
  # The `git ls-files -z` loads the files in the RubyGem that have been added into git.
  spec.files = Dir.chdir(File.expand_path('..', __FILE__)) do
    `git ls-files -z`.split("\x0").reject { |f| f.match(%r{^(test|spec|features)/}) }
  end
  spec.bindir = "exe"
  spec.executables = spec.files.grep(%r{^exe/}) { |f| File.basename(f) }
  spec.require_paths = ["lib"]
end
