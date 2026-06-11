# -*- mode: Python -*-
# airavata tenant — deploys into the shared devstack VM. Prereq: ./devstack/devstack setup.
# Tiltfiles are Starlark, not Python: no `import`; `os` is built in (getenv/putenv only).
PROFILE = os.getenv('DEVSTACK_PROFILE', 'airavata')
os.putenv('DOCKER_HOST', 'unix://%s/.colima/%s/docker.sock' % (os.getenv('HOME'), PROFILE))

local_resource('devstack-ensure', cmd='./devstack/devstack ensure', labels=['platform'])

local_resource('build', cmd='mvn install -DskipTests -Dmaven.test.skip=true -T4 -q',
               deps=['airavata-server/pom.xml'], labels=['build'])

docker_build('airavata-server:dev', '.', dockerfile='Dockerfile',
             only=['Dockerfile', 'airavata-server/target/airavata-server-0.21-SNAPSHOT.jar', 'conf'])

docker_compose('./compose.yml')
dc_resource('airavata-server', resource_deps=['devstack-ensure', 'build'], labels=['airavata'],
            links=[link('https://api.airavata.host/docs', 'API Docs'),
                   link('https://auth.airavata.host', 'Keycloak')])

if os.getenv('DEVSTACK_MODE') != 'remote':
    for s in ['db', 'rabbitmq', 'zookeeper', 'kafka', 'keycloak', 'sftp']:
        dc_resource(s, resource_deps=['devstack-ensure'], labels=['infra'])
