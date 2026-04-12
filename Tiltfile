# Airavata Development Tiltfile
# Usage: tilt up

# --- Generate dev SSH keypair for SFTP container (idempotent) ---
local(
    'mkdir -p conf/sftp && '
    'test -f conf/sftp/id_rsa || '
    'ssh-keygen -t rsa -b 2048 -f conf/sftp/id_rsa -N "" -q && '
    'echo "Generated dev SSH keypair at conf/sftp/id_rsa"',
    quiet=True,
)

# --- Infrastructure (from compose.yml) ---
docker_compose('./compose.yml')

# --- Build ---
local_resource(
    'build',
    cmd='mvn install -DskipTests -Dmaven.test.skip=true -T4 -q',
    deps=[
        'airavata-server/pom.xml',
    ],
    ignore=['**/target/**'],
    trigger_mode=TRIGGER_MODE_AUTO,
    labels=['build'],
)

# --- Airavata Server (unified: gRPC + REST via Armeria on port 9090) ---
local_resource(
    'airavata-server',
    serve_cmd='java -jar airavata-server/target/airavata-server-0.21-SNAPSHOT.jar',
    readiness_probe=probe(
        http_get=http_get_action(port=9090, path='/internal/actuator/health'),
        initial_delay_secs=30,
        period_secs=5,
        timeout_secs=5,
    ),
    resource_deps=['build', 'db', 'rabbitmq', 'zookeeper', 'kafka', 'keycloak'],
    links=[
        link('http://localhost:9090/docs', 'API Docs (Armeria DocService)'),
        link('http://localhost:9090/internal/actuator/health', 'Health'),
    ],
    labels=['airavata'],
)

# --- Integration Tests (manual trigger) ---
local_resource(
    'integration-tests',
    cmd='mvn test -pl airavata-api -Dgroups=runtime -Dsurefire.excludedGroups="" -q',
    resource_deps=['airavata-server'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL,
    labels=['tests'],
)
