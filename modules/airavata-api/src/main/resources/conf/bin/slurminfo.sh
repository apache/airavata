#!/bin/sh
# partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts
set -eu

have() { command -v "$1" >/dev/null 2>&1; }

# stable, unique, alphabetic, comma-joined
csv_join_sorted() { LC_ALL=C sort -u | paste -sd, -; }

# --- partitions --------------------------------------------------------------
parts="$(
  sinfo -h -o "%P" 2>/dev/null \
  | sed 's/\*//g; s/:.*$//; /^$/d' \
  | LC_ALL=C sort -u || true
)"
[ -n "${parts:-}" ] || exit 0

# --- accounting snapshot (optional) -----------------------------------------
acct_ok=0
assoc=""
if have sacctmgr && sacctmgr -nP show assoc where user="$USER" format=Account >/dev/null 2>&1; then
  acct_ok=1
  # Account|Partition|QOS|MaxTRES|GrpTRES
  assoc="$(
    sacctmgr -nP show assoc where user="$USER" format=Account,Partition,QOS,MaxTRES,GrpTRES 2>/dev/null \
    | sed '/^$/d' || true
  )"
fi

gpu_capable_accounts() {
  # stdin: assoc lines -> stdout: sorted unique GPU-capable accounts (conservative)
  awk -F'|' '
    function has_gpu(s) { return (s ~ /(^|,| )gpu(=|:|,| |$)/) || (s ~ /gres\/gpu(=|:|,| |$)/) }
    {
      acct=$1; qos=$3; max=$4; grp=$5
      if (acct=="") next
      if (has_gpu(max) || has_gpu(grp) || tolower(qos) ~ /gpu/) ok[acct]=1
    }
    END { for (a in ok) print a }
  ' | LC_ALL=C sort -u
}

GPU_ACCTS=""
if [ "$acct_ok" -eq 1 ] && [ -n "${assoc:-}" ]; then
  GPU_ACCTS="$(printf "%s\n" "$assoc" | gpu_capable_accounts || true)"
fi

accounts_for_partition() {
  # $1 partition, $2 is_gpu(0/1) -> CSV accounts (sorted). Empty => none/unknown.
  p="$1"; is_gpu="$2"
  [ "$acct_ok" -eq 1 ] || { printf "%s" ""; return 0; }

  # Prefer partition-specific assoc; else global (Partition blank)
  spec="$(printf "%s\n" "$assoc" | awk -F'|' -v p="$p" '$2==p{print $1}' | LC_ALL=C sort -u)"
  if [ -n "${spec:-}" ]; then
    cand="$spec"
  else
    cand="$(printf "%s\n" "$assoc" | awk -F'|' '$2==""{print $1}' | LC_ALL=C sort -u)"
  fi
  [ -n "${cand:-}" ] || { printf "%s" ""; return 0; }

  if [ "$is_gpu" -eq 1 ]; then
    [ -n "${GPU_ACCTS:-}" ] || { printf "%s" ""; return 0; }
    # intersect sorted lists using comm
    out="$(comm -12 /dev/fd/3 /dev/fd/4 3<<EOF 4<<EOF2
$GPU_ACCTS
EOF
$cand
EOF2
)"
    [ -n "${out:-}" ] && printf "%s\n" "$out" | csv_join_sorted || printf "%s" ""
  else
    printf "%s\n" "$cand" | csv_join_sorted
  fi
}

max_gpus_in_partition() {
  # $1 partition -> max GPUs per node from %G, avoiding double-counting
  p="$1"
  sinfo -h -p "$p" -N -o "%G" 2>/dev/null \
  | awk '
      function tok_gpu(tok,    m) {
        if (tok !~ /(^|[^[:alnum:]_])gpu([^[:alnum:]_]|$)/) return 0
        if (match(tok, /:([0-9]+)/, m)) return m[1]+0
        return 1
      }
      function gres_gpu_max(gres,   n,items,i,g,best) {
        best=0
        if (gres=="" || gres=="(null)") return 0
        n=split(gres, items, ",")
        for (i=1;i<=n;i++) { g=tok_gpu(items[i]); if (g>best) best=g }
        return best
      }
      { g=gres_gpu_max($0); if (g>max) max=g }
      END{ print max+0 }
    '
}

printf "%s\n" "partition|nodes|max_cpus_per_node|max_gpus_per_node|accounts"

for p in $parts; do
  nodes="$(sinfo -h -p "$p" -o "%D" 2>/dev/null | awk '{s+=$1} END{print s+0}')"
  [ "${nodes:-0}" -gt 0 ] || continue

  maxcpus="$(sinfo -h -p "$p" -N -o "%c" 2>/dev/null | awk '$1>m{m=$1} END{print m+0}')"
  maxgpus="$(max_gpus_in_partition "$p" 2>/dev/null || echo 0)"

  is_gpu=0
  [ "${maxgpus:-0}" -gt 0 ] && is_gpu=1

  accts=""
  if [ "$acct_ok" -eq 1 ]; then
    accts="$(accounts_for_partition "$p" "$is_gpu")"
    [ -n "${accts:-}" ] || continue
  fi

  printf "%s|%s|%s|%s|%s\n" \
    "$p" "$nodes" "${maxcpus:-0}" "${maxgpus:-0}" "${accts:-}"
done
