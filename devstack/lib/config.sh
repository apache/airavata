# devstack/lib/config.sh — load/persist devstack config and derive paths.
DEVSTACK_HOME="${DEVSTACK_HOME:-$HOME/.airavata-devstack}"
DEVSTACK_CONF="$DEVSTACK_HOME/config.env"

devstack_load_config() {
  mkdir -p "$DEVSTACK_HOME/certs" "$DEVSTACK_HOME/traefik/dynamic"
  [ -f "$DEVSTACK_CONF" ] && . "$DEVSTACK_CONF"
  : "${DEVSTACK_PROFILE:=airavata}"
  : "${DEVSTACK_PROJECT:=airavata}"
  : "${DEVSTACK_TLD:=airavata.host}"
  DEVSTACK_SOCK="$HOME/.colima/$DEVSTACK_PROFILE/docker.sock"
  export DOCKER_HOST="unix://$DEVSTACK_SOCK"
  export DEVSTACK_PROFILE DEVSTACK_PROJECT DEVSTACK_TLD DEVSTACK_SOCK DEVSTACK_HOME
}

devstack_save_config() {
  cat > "$DEVSTACK_CONF" <<EOF
DEVSTACK_PROFILE=$DEVSTACK_PROFILE
DEVSTACK_PROJECT=$DEVSTACK_PROJECT
DEVSTACK_TLD=$DEVSTACK_TLD
EOF
  echo "wrote $DEVSTACK_CONF"
}

devstack_os() { case "$(uname -s)" in Darwin) echo macos;; Linux) echo linux;; *) echo unknown;; esac; }
