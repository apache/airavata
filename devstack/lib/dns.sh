# devstack/lib/dns.sh — map *.<tld> -> 127.0.0.1 (host-aware). Run from `setup` (sudo).
_dnsmasq_conf_macos() {
  local prefix; prefix="$(brew --prefix)"
  local d="$prefix/etc/dnsmasq.d"; mkdir -p "$d"
  # per-project drop-in (never edits the shared main conf)
  printf 'address=/%s/127.0.0.1\n' "$DEVSTACK_TLD" > "$d/$DEVSTACK_TLD.conf"
  grep -q "conf-dir=$d" "$prefix/etc/dnsmasq.conf" 2>/dev/null || \
    echo "conf-dir=$d,*.conf" >> "$prefix/etc/dnsmasq.conf"
  sudo brew services restart dnsmasq
  sudo mkdir -p /etc/resolver
  printf 'nameserver 127.0.0.1\n' | sudo tee "/etc/resolver/$DEVSTACK_TLD" >/dev/null
  # scrub any stale apex line in /etc/hosts so nsswitch 'files' can't shadow it
  if grep -qE "[[:space:]]$DEVSTACK_TLD($|[[:space:]])" /etc/hosts; then
    sudo sed -i '' "/[[:space:]]$DEVSTACK_TLD\$/d" /etc/hosts || true
  fi
  sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder 2>/dev/null || true
}

# Linux: detect the DNS manager and apply the matching recipe (-> 127.0.0.1).
_dnsmasq_conf_linux() {
  local d=/etc/dnsmasq.d; sudo mkdir -p "$d"
  printf 'address=/%s/127.0.0.1\n' "$DEVSTACK_TLD" | sudo tee "$d/$DEVSTACK_TLD.conf" >/dev/null
  if grep -q '^dns=dnsmasq' /etc/NetworkManager/NetworkManager.conf 2>/dev/null; then
    # NetworkManager's built-in dnsmasq plugin owns resolution.
    sudo mkdir -p /etc/NetworkManager/dnsmasq.d
    printf 'address=/%s/127.0.0.1\n' "$DEVSTACK_TLD" | \
      sudo tee "/etc/NetworkManager/dnsmasq.d/$DEVSTACK_TLD.conf" >/dev/null
    sudo systemctl reload NetworkManager
  elif systemctl is-active --quiet systemd-resolved; then
    # split-DNS: dnsmasq on a dedicated loopback; route only this TLD to it.
    sudo sed -i 's/^#\?listen-address=.*/listen-address=127.0.0.2/' /etc/dnsmasq.conf 2>/dev/null || \
      echo 'listen-address=127.0.0.2' | sudo tee -a /etc/dnsmasq.conf >/dev/null
    grep -q '^bind-interfaces' /etc/dnsmasq.conf || echo 'bind-interfaces' | sudo tee -a /etc/dnsmasq.conf >/dev/null
    sudo systemctl restart dnsmasq
    sudo mkdir -p /etc/systemd/resolved.conf.d
    printf '[Resolve]\nDNS=127.0.0.2\nDomains=~%s\n' "$DEVSTACK_TLD" | \
      sudo tee "/etc/systemd/resolved.conf.d/$DEVSTACK_TLD.conf" >/dev/null
    sudo systemctl restart systemd-resolved
  else
    echo "WARN: unknown Linux DNS manager; add 'address=/$DEVSTACK_TLD/127.0.0.1' to dnsmasq and restart it manually" >&2
  fi
  # scrub stale apex /etc/hosts line
  sudo sed -i "/[[:space:]]$DEVSTACK_TLD\$/d" /etc/hosts 2>/dev/null || true
}

devstack_dns() {
  case "$(devstack_os)" in
    macos) _dnsmasq_conf_macos ;;
    linux) _dnsmasq_conf_linux ;;
    *) echo "unsupported OS" >&2; exit 1 ;;
  esac
}
