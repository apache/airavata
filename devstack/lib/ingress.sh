# devstack/lib/ingress.sh — shared Traefik bring-up + per-project registration.
INGRESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../traefik" && pwd)"

_dc() { DEVSTACK_HOME="$DEVSTACK_HOME" docker compose -f "$INGRESS_DIR/compose.yml" "$@"; }

ingress_up() {
  cp "$INGRESS_DIR/traefik.yml" "$DEVSTACK_HOME/traefik/traefik.yml"
  # default cert for no-SNI / unknown-SNI clients
  cat > "$DEVSTACK_HOME/traefik/dynamic/_tls-default.yml" <<EOF
tls:
  stores:
    default:
      defaultCertificate:
        certFile: /certs/$DEVSTACK_TLD.pem
        keyFile: /certs/$DEVSTACK_TLD-key.pem
EOF
  _dc up -d
}

ingress_register_project() {
  # 1) per-project cert entry (additive; never edits another project's file)
  cat > "$DEVSTACK_HOME/traefik/dynamic/$DEVSTACK_PROJECT.yml" <<EOF
tls:
  certificates:
    - certFile: /certs/$DEVSTACK_TLD.pem
      keyFile: /certs/$DEVSTACK_TLD-key.pem
EOF
  # 2) project network (idempotent) + connect Traefik to it WITH the dev hostname
  #    aliases, so in-VM containers resolve <name>.$DEVSTACK_TLD to Traefik via
  #    Docker's embedded DNS (server -> auth.airavata.host, portal -> auth/api, ...).
  docker network inspect "$DEVSTACK_PROJECT-devstack" >/dev/null 2>&1 || \
    docker network create --attachable "$DEVSTACK_PROJECT-devstack"
  docker network connect \
    --alias "api.$DEVSTACK_TLD"      --alias "auth.$DEVSTACK_TLD" \
    --alias "gateway.$DEVSTACK_TLD"  --alias "adminer.$DEVSTACK_TLD" \
    "$DEVSTACK_PROJECT-devstack" airavata-devstack-traefik 2>/dev/null || true
}
