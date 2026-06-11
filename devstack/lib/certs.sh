# devstack/lib/certs.sh — mkcert CA trust + per-project wildcard+apex cert.
devstack_certs() {
  command -v mkcert >/dev/null || { echo "ERROR: mkcert not installed" >&2; exit 1; }
  mkcert -install   # idempotent; trusts the CA in system + NSS stores
  cp "$(mkcert -CAROOT)/rootCA.pem" "$DEVSTACK_HOME/certs/rootCA.pem"
  local cert="$DEVSTACK_HOME/certs/$DEVSTACK_TLD.pem"
  local key="$DEVSTACK_HOME/certs/$DEVSTACK_TLD-key.pem"
  if [ ! -f "$cert" ]; then
    # wildcard does NOT cover the apex — list both SANs.
    mkcert -cert-file "$cert" -key-file "$key" "*.$DEVSTACK_TLD" "$DEVSTACK_TLD"
    echo "generated cert for *.$DEVSTACK_TLD + $DEVSTACK_TLD"
  fi
}
