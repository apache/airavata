# devstack/lib/commands.sh — top-level verbs.
devstack_setup() {
  [ "$(devstack_os)" = unknown ] && { echo "unsupported OS"; exit 1; }
  devstack_save_config
  if [ "$(devstack_os)" = macos ]; then
    command -v brew >/dev/null || { echo "install Homebrew first"; exit 1; }
    brew list colima >/dev/null 2>&1 || brew install colima docker dnsmasq mkcert
  else
    sudo apt-get update -y && sudo apt-get install -y dnsmasq mkcert libnss3-tools
    command -v colima >/dev/null || { echo "install colima + docker for Linux first"; exit 1; }
  fi
  colima_running || colima_create
  devstack_certs
  devstack_dns          # sudo steps live here
  ingress_up
  ingress_register_project
  devstack_verify
  echo "devstack setup complete — now run: tilt up"
}

devstack_ensure() {
  colima_require
  ingress_up
  ingress_register_project
  devstack_verify
}

devstack_status() {
  echo "profile=$DEVSTACK_PROFILE project=$DEVSTACK_PROJECT tld=$DEVSTACK_TLD"
  colima status -p "$DEVSTACK_PROFILE" 2>&1 | sed 's/^/  /' || true
  docker ps --filter name=airavata-devstack-traefik --format '  traefik: {{.Status}}' 2>/dev/null || true
}

devstack_down() {  # stop the shared ingress + the VM (GLOBAL — affects all projects)
  DEVSTACK_HOME="$DEVSTACK_HOME" docker compose -f "$(dirname "${BASH_SOURCE[0]}")/../traefik/compose.yml" down 2>/dev/null || true
  colima stop -p "$DEVSTACK_PROFILE" || true
}

devstack_reset() {  # GLOBAL destructive — wipes ALL projects' state
  echo "WARNING: deletes the shared VM and ALL projects' data on it."
  read -r -p "type the profile name to confirm: " c
  [ "$c" = "$DEVSTACK_PROFILE" ] && colima delete -p "$DEVSTACK_PROFILE" -f || echo "aborted"
}
