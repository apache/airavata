# devstack/lib/colima.sh — colima VM lifecycle for the shared substrate.
# NOTE: no --network-address (macOS-only + root-gated); ingress is via 127.0.0.1 forwarding.
DEVSTACK_CPU="${DEVSTACK_CPU:-4}"
DEVSTACK_MEM="${DEVSTACK_MEM:-8}"
DEVSTACK_DISK="${DEVSTACK_DISK:-60}"

colima_running() { colima status -p "$DEVSTACK_PROFILE" >/dev/null 2>&1; }

colima_create() {
  echo "creating colima VM '$DEVSTACK_PROFILE' (cpu=$DEVSTACK_CPU mem=${DEVSTACK_MEM}g disk=${DEVSTACK_DISK}g)"
  colima start -p "$DEVSTACK_PROFILE" \
    --cpu "$DEVSTACK_CPU" --memory "$DEVSTACK_MEM" --disk "$DEVSTACK_DISK" \
    --mount-inotify
}

colima_start_if_stopped() {
  if colima_running; then echo "colima '$DEVSTACK_PROFILE' already running"; else
    echo "starting colima '$DEVSTACK_PROFILE'"; colima start -p "$DEVSTACK_PROFILE"; fi
}

colima_require() {
  colima_running || { echo "ERROR: colima '$DEVSTACK_PROFILE' is not running. Run: ./devstack/devstack setup" >&2; exit 1; }
  [ -S "$DEVSTACK_SOCK" ] || { echo "ERROR: docker socket $DEVSTACK_SOCK missing" >&2; exit 1; }
}
