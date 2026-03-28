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
        'airavata-api/file-server/src',
        'airavata-api/file-server/pom.xml',
        'airavata-api/agent-service/src',
        'airavata-api/agent-service/pom.xml',
        'airavata-api/research-service/src',
        'airavata-api/research-service/pom.xml',
        'airavata-server/thrift/src',
        'airavata-server/thrift/pom.xml',
        'airavata-server/rest/src',
        'airavata-server/rest/pom.xml',
        'airavata-server/grpc/src',
        'airavata-server/grpc/pom.xml',
        'airavata-server/src',
        'airavata-server/pom.xml',
    ],
    labels=['build'],
)

# --- Airavata Server (unified: Thrift + REST + gRPC) ---
local_resource(
    'airavata-server',
    serve_cmd='java -jar airavata-server/target/airavata-server-0.21-SNAPSHOT.jar',
    readiness_probe=probe(
        http_get=http_get_action(port=18889, path='/actuator/health'),
        initial_delay_secs=20,
        period_secs=5,
        timeout_secs=5,
    ),
    resource_deps=['build', 'db', 'rabbitmq', 'zookeeper', 'kafka', 'keycloak'],
    links=[
        link('http://localhost:18889/swagger-ui.html', 'Swagger UI'),
        link('http://localhost:18889/actuator/health', 'Health'),
    ],
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
