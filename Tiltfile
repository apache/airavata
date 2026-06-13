# -*- mode: Python -*-
# airavata tenant — deploys into the shared devstack VM. Prereq: ./devstack/devstack setup.
# Tiltfiles are Starlark, not Python: no `import`; `os` is built in (getenv/putenv only).
PROFILE = os.getenv('DEVSTACK_PROFILE', 'airavata')
os.putenv('DOCKER_HOST', 'unix://%s/.colima/%s/docker.sock' % (os.getenv('HOME'), PROFILE))

local_resource('devstack-ensure', cmd='./devstack/devstack ensure', labels=['platform'])

# The devstack SSH keypair (conf/sftp/id_rsa[.pub]) is a committed, fixed dev-only key: the
# server reads the private key via the ./conf bind mount, the sftp and slurm containers mount
# the public key as authorized_keys, and conf/db/seed.sql carries the matching credential
# (encrypted with the committed keystore). It must NOT be regenerated, or the seed would no
# longer decrypt to it.

local_resource('build', cmd='mvn install -DskipTests -Dmaven.test.skip=true -T4 -q',
               deps=['airavata-server/pom.xml'], labels=['build'])

docker_build('airavata-server:dev', '.', dockerfile='Dockerfile',
             only=['Dockerfile', 'airavata-server/target/airavata-server-0.21-SNAPSHOT.jar', 'conf'])

# Self-contained SLURM cluster image (controller/login + compute + slurmdbd roles).
# Built from conf/slurm; rebuilds only when those files change.
docker_build('airavata-slurm:dev', './conf/slurm', dockerfile='./conf/slurm/Dockerfile')

docker_compose('./compose.yml')
dc_resource('airavata-server', resource_deps=['devstack-ensure', 'build'], labels=['airavata'],
            links=[link('https://api.airavata.host/docs', 'API Docs'),
                   link('https://auth.airavata.host', 'Keycloak')])

if os.getenv('DEVSTACK_MODE') != 'remote':
    for s in ['db', 'keycloak', 'sftp']:
        dc_resource(s, resource_deps=['devstack-ensure'], labels=['infra'])
    # SLURM cluster resources. slurm-dbmysql has no image to build; the rest share
    # the airavata-slurm:dev image. Grouped under a 'slurm' label for clarity.
    for s in ['slurm-dbmysql', 'slurmdbd', 'slurmctld', 'c1', 'c2']:
        dc_resource(s, resource_deps=['devstack-ensure'], labels=['slurm'])
