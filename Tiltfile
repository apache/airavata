# Airavata Development Tiltfile — single-container (Docker-in-Docker) stack.
# `tilt up`  boots ONE host container (airavata-dind) that runs its own dockerd +
#            registry; all infra and the Airavata server run inside it.
# `tilt down` removes that one container and everything inside it (ephemeral).

DIND = 'airavata-dind'
REPO = '/workspace/airavata'                                   # repo path INSIDE DinD
COMPOSE = 'docker exec %s docker compose -f %s/compose.yml' % (DIND, REPO)

# Friendly hostnames (resolve via *.localhost loopback + Traefik Host routing).
HOSTNAMES = {
    'api.airavata.localhost':      'airavata-server',
    'auth.airavata.localhost':     'keycloak',
    'rabbitmq.airavata.localhost': 'rabbitmq',
    'adminer.airavata.localhost':  'adminer',
}

# --- Generate dev SSH keypair for SFTP container (idempotent) ---
local(
    'mkdir -p conf/sftp && ' +
    'test -f conf/sftp/id_rsa || ' +
    'ssh-keygen -t rsa -b 2048 -f conf/sftp/id_rsa -N "" -q && ' +
    'echo "Generated dev SSH keypair at conf/sftp/id_rsa"',
    quiet=True,
)

# --- Generate dev TLS cert for *.airavata.localhost via mkcert (idempotent) ---
# Requires a one-time `brew install mkcert && mkcert -install` so the local CA is
# trusted by your browser. The cert + CA are written under conf/traefik/certs/
# (gitignored). The CA is also imported into the server container's JVM truststore.
local(
    """
mkdir -p conf/traefik/certs
if [ ! -f conf/traefik/certs/airavata.localhost.crt ]; then
  if command -v mkcert >/dev/null 2>&1; then
    mkcert -cert-file conf/traefik/certs/airavata.localhost.crt -key-file conf/traefik/certs/airavata.localhost.key "*.airavata.localhost" airavata.localhost localhost 127.0.0.1 ::1
    cp "$(mkcert -CAROOT)/rootCA.pem" conf/traefik/certs/rootCA.pem
    echo "Generated dev TLS cert for *.airavata.localhost"
  else
    echo "WARN: mkcert not installed -- HTTPS disabled. Run: brew install mkcert && mkcert -install"
  fi
fi
""",
    quiet=True,
)

# --- The single host container: Docker-in-Docker host (Tilt resource: airavata-dind) ---
docker_compose('./compose.dind.yml')
dc_resource('airavata-dind', labels=['platform'])   # group under the platform bucket (not "unlabeled")

# --- Gate: block until the inner dockerd actually accepts connections ---
# The dind container can be "running" a beat before its daemon is ready; Tilt does not
# wait on the compose healthcheck for resource_deps, so gate explicitly here. Single
# quotes only (no nested double-quotes / &&) so Tilt's shell parses it cleanly.
local_resource(
    'dind-ready',
    cmd="docker exec %s sh -c 'until docker info >/dev/null 2>&1; do sleep 1; done'" % DIND,
    resource_deps=['airavata-dind'],
    labels=['platform'],
)

# readiness probe: an inner container reports docker health == healthy
def inner_health(container):
    return probe(
        exec=exec_action(command=[
            'sh', '-c',
            "docker exec %s docker inspect -f '{{.State.Health.Status}}' %s | grep -q healthy" % (DIND, container),
        ]),
        initial_delay_secs=5, period_secs=5, timeout_secs=10,
    )

# bring up one inner compose service as its own Tilt resource (logs + readiness)
def inner_service(name, container, deps=[], labels=['infra']):
    local_resource(
        name,
        cmd='%s up -d %s' % (COMPOSE, name),
        serve_cmd='%s logs -f --no-log-prefix %s' % (COMPOSE, name),
        readiness_probe=inner_health(container),
        resource_deps=['dind-ready'] + deps,
        labels=labels,
    )

# --- Platform services inside DinD ---
# registry comes up first and creates the shared `airavata_default` compose network;
# every other inner service depends on it so the parallel `up -d` calls don't race on
# network creation.
inner_service('registry', 'airavata-registry', labels=['platform'])
inner_service('traefik',  'airavata-traefik',  deps=['registry'], labels=['platform'])

# --- Infrastructure inside DinD ---
inner_service('db',        'airavata-db',        deps=['registry'])
inner_service('rabbitmq',  'airavata-rabbitmq',  deps=['registry'])
inner_service('zookeeper', 'airavata-zookeeper', deps=['registry'])
inner_service('kafka',     'airavata-kafka',     deps=['registry'])
inner_service('keycloak',  'airavata-keycloak',  deps=['registry'])
inner_service('sftp',      'airavata-sftp',      deps=['registry'])

# --- Build the server fat JAR on the host (incremental) ---
# NOTE: watching the full airavata-api/airavata-server source trees overflows macOS
# fsnotify (mvn writes thousands of files into target/), which thrashes the build in a
# re-trigger loop. Watch a narrow signal instead; use `tilt trigger build` (or the
# Tilt UI) to force a rebuild after source edits. See docs for the auto-rebuild loop.
local_resource(
    'build',
    cmd='mvn install -DskipTests -Dmaven.test.skip=true -T4 -q',
    deps=['airavata-server/pom.xml'],
    labels=['build'],
)

# --- Server image: build in DinD -> push to registry -> pull -> run ---
local_resource(
    'airavata-server',
    cmd=' && '.join([
        '%s build airavata-server' % COMPOSE,
        '%s push airavata-server' % COMPOSE,
        '%s pull airavata-server' % COMPOSE,
        '%s up -d --force-recreate airavata-server' % COMPOSE,
    ]),
    serve_cmd='%s logs -f --no-log-prefix airavata-server' % COMPOSE,
    # /actuator/health (Spring Actuator, bridged onto Armeria) reflects DB + messaging
    # reachability — a real readiness signal, not just "Armeria is serving".
    readiness_probe=probe(
        http_get=http_get_action(port=9090, path='/actuator/health'),
        initial_delay_secs=30, period_secs=5, timeout_secs=5,
    ),
    deps=['airavata-server/target/airavata-server-0.21-SNAPSHOT.jar'],
    resource_deps=['dind-ready', 'build', 'registry', 'traefik',
                   'db', 'rabbitmq', 'zookeeper', 'kafka', 'keycloak'],
    links=[
        link('http://api.airavata.localhost/docs', 'API Docs'),
        link('http://api.airavata.localhost/internal/actuator/health', 'Health'),
        link('http://auth.airavata.localhost', 'Keycloak'),
        link('http://rabbitmq.airavata.localhost', 'RabbitMQ'),
    ],
    labels=['airavata'],
)

# --- Integration tests (manual; run inside the DinD network) ---
local_resource(
    'integration-tests',
    cmd=' '.join([
        'docker exec %s docker run --rm --network airavata_default' % DIND,
        '-v %s:/work -w /work' % REPO,
        '-e SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/airavata',
        '-e KAFKA_BROKER_URL=kafka:9092',
        '-e ZOOKEEPER_SERVER_CONNECTION=zookeeper:2181',
        '-e RABBITMQ_BROKER_URL=amqp://airavata:airavata@rabbitmq:5672',
        'maven:3.9-eclipse-temurin-17',
        'mvn test -pl airavata-api -Dgroups=runtime -Dsurefire.excludedGroups="" -q',
    ]),
    resource_deps=['airavata-server'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL,
    labels=['tests'],
)
