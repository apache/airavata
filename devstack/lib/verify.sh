# devstack/lib/verify.sh — hard-fail gate run at the end of ensure.
devstack_verify() {
  local host="api.$DEVSTACK_TLD" ok=1
  case "$(devstack_os)" in
    macos)
      dscacheutil -q host -a name "$host" | grep -q '127.0.0.1' || { echo "DNS FAIL: $host !-> 127.0.0.1 (use dscacheutil, not dig)"; ok=0; } ;;
    linux)
      getent hosts "$host" | grep -q '127.0.0.1' || { echo "DNS FAIL: $host !-> 127.0.0.1"; ok=0; } ;;
  esac
  nc -z -w2 127.0.0.1 443 || { echo "INGRESS FAIL: nothing on 127.0.0.1:443"; ok=0; }
  [ "$ok" = 1 ] && echo "devstack verify: OK ($host -> 127.0.0.1, Traefik :443 up)"
  [ "$ok" = 1 ]
}
