# Airavata Development Tiltfile
# Usage: tilt up

# --- Infrastructure (from compose.yml) ---
docker_compose('./compose.yml')

# --- Build ---
local_resource(
    'build',
    cmd='mvn package -DskipTests -T4 -pl airavata-api,airavata-thrift-server -am -q',
    deps=[
        'airavata-api/src',
        'airavata-api/pom.xml',
        'airavata-thrift-server/src',
        'airavata-thrift-server/pom.xml',
    ],
    labels=['build'],
)

# --- Airavata Server (classpath mode) ---
local_resource(
    'airavata-server',
    serve_cmd=' '.join([
        'java',
        '-cp', 'airavata-thrift-server/target/classes:airavata-api/target/classes:airavata-api/target/dependency/*',
        '-Dlog4j.configurationFile=airavata-api/src/main/resources/log4j2.xml',
        '-Dairavata.config.dir=airavata-api/src/main/resources',
        'org.apache.airavata.api.server.AiravataServer',
    ]),
    readiness_probe=probe(
        http_get=http_get_action(port=9097, path='/health/services'),
        period_secs=5,
        timeout_secs=5,
        initial_delay_secs=15,
    ),
    resource_deps=['build', 'db', 'rabbitmq', 'zookeeper', 'kafka', 'keycloak'],
    labels=['airavata'],
)

# --- Integration Tests ---
local_resource(
    'integration-tests',
    cmd='mvn test -pl integration-tests -Dgroups=integration -am -q',
    resource_deps=['airavata-server'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL,
    labels=['tests'],
)
