# Airavata Development Tiltfile
# Usage: tilt up

# --- Infrastructure (from compose.yml) ---
docker_compose('./compose.yml')

# --- Build ---
local_resource(
    'build',
    cmd='mvn install -DskipTests -T4 -am -q',
    deps=[
        'airavata-api/src',
        'airavata-api/pom.xml',
        'airavata-thrift-server/src',
        'airavata-thrift-server/pom.xml',
        'modules/file-server/src',
        'modules/file-server/pom.xml',
        'modules/agent-framework/agent-service/src',
        'modules/agent-framework/agent-service/pom.xml',
        'modules/research-framework/research-service/src',
        'modules/research-framework/research-service/pom.xml',
        'modules/restproxy/src',
        'modules/restproxy/pom.xml',
    ],
    labels=['build'],
)

# --- Airavata Thrift Server (port 8930 thrift, 9097 monitoring) ---
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

# --- Research Service (port 18889 HTTP, 19908 gRPC) ---
local_resource(
    'research-service',
    serve_cmd='mvn spring-boot:run -pl modules/research-framework/research-service -q',
    readiness_probe=probe(
        http_get=http_get_action(port=18889, path='/actuator/health'),
        period_secs=5,
        timeout_secs=5,
        initial_delay_secs=20,
    ),
    resource_deps=['build', 'airavata-server', 'db', 'keycloak'],
    labels=['airavata'],
)

# --- Agent Service (port 18880 HTTP, 19900 gRPC) ---
local_resource(
    'agent-service',
    serve_cmd='mvn spring-boot:run -pl modules/agent-framework/agent-service -q',
    readiness_probe=probe(
        http_get=http_get_action(port=18880, path='/actuator/health'),
        period_secs=5,
        timeout_secs=5,
        initial_delay_secs=20,
    ),
    resource_deps=['build', 'airavata-server', 'db'],
    labels=['airavata'],
)

# --- File Server (port 8050) ---
local_resource(
    'file-server',
    serve_cmd='mvn spring-boot:run -pl modules/file-server -q',
    readiness_probe=probe(
        http_get=http_get_action(port=8050, path='/actuator/health'),
        period_secs=5,
        timeout_secs=5,
        initial_delay_secs=15,
    ),
    resource_deps=['build', 'airavata-server'],
    labels=['airavata'],
)

# --- REST Proxy (port 8082) ---
local_resource(
    'rest-proxy',
    serve_cmd='mvn spring-boot:run -pl modules/restproxy -q',
    readiness_probe=probe(
        http_get=http_get_action(port=8082, path='/actuator/health'),
        period_secs=5,
        timeout_secs=5,
        initial_delay_secs=15,
    ),
    resource_deps=['build', 'airavata-server', 'kafka'],
    labels=['airavata'],
)

# --- Integration Tests (manual trigger) ---
local_resource(
    'integration-tests',
    cmd='mvn test -pl integration-tests -Dgroups=integration -am -q',
    resource_deps=['airavata-server'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL,
    labels=['tests'],
)
