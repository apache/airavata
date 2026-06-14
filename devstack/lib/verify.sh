# devstack/lib/verify.sh — hard-fail gate run at the end of ensure.
devstack_verify() {
  local host="api.$DEVSTACK_TLD" ok=1
  case "$(devstack_os)" in
    macos)
      dscacheutil -q host -a name "$host" | grep -q '127.0.0.1' || { echo "DNS FAIL: $host !-> 127.0.0.1 (use dscacheutil, not dig)"; ok=0; } ;;
    linux)
      getent hosts "$host" | grep -q '127.0.0.1' || { echo "DNS FAIL: $host !-> 127.0.0.1"; ok=0; } ;;
  esac
  # Traefik may have only just started (fresh image pull / cold boot); retry briefly so
  # a not-yet-ready ingress doesn't spuriously fail the gate.
  local i ingress=0
  for i in $(seq 1 15); do
    nc -z -w2 127.0.0.1 443 && { ingress=1; break; }
    sleep 1
  done
  [ "$ingress" = 1 ] || { echo "INGRESS FAIL: nothing on 127.0.0.1:443 after retries"; ok=0; }
  [ "$ok" = 1 ] && echo "devstack verify: OK ($host -> 127.0.0.1, Traefik :443 up)"
  [ "$ok" = 1 ]
}
